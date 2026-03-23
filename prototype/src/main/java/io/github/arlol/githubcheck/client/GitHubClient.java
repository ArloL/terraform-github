package io.github.arlol.githubcheck.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GitHubClient {

	// Response records — the parsed shapes of GitHub API responses
	public record RepoSummary(
			String name,
			boolean archived,
			String visibility
	) {
	}

	public record RepoDetails(
			String description,
			String homepageUrl,
			boolean hasIssues,
			boolean hasProjects,
			boolean hasWiki,
			String defaultBranch,
			boolean allowMergeCommit,
			boolean allowSquashMerge,
			boolean allowAutoMerge,
			boolean deleteBranchOnMerge,
			boolean secretScanning,
			boolean secretScanningPushProtection
	) {
	}

	public record BranchProtection(
			boolean enforceAdmins,
			boolean requiredLinearHistory,
			boolean allowForcePushes,
			boolean requiredStatusChecksStrict,
			List<String> requiredStatusCheckContexts
	) {
	}

	public record WorkflowPermissions(
			String defaultPermissions,
			boolean canApprovePullRequestReviews
	) {
	}

	public record Pages(
			BuildType buildType
	) {

		public enum BuildType {

			WORKFLOW, LEGACY, NULL;

			public static BuildType valueOfRaw(String buildType) {
				if (buildType == null || buildType.isBlank()) {
					return BuildType.NULL;
				}
				return valueOf(buildType.toUpperCase());
			}

		}

	}

	private final String baseUrl;
	private final String token;
	private final HttpClient http;
	private final ObjectMapper mapper;

	public GitHubClient(String token) {
		this("https://api.github.com", token);
	}

	GitHubClient(String baseUrl, String token) {
		this.baseUrl = baseUrl;
		this.token = token;
		this.http = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_2)
				.connectTimeout(Duration.ofSeconds(10))
				.build();
		this.mapper = new ObjectMapper();
	}

	public List<RepoSummary> listOrgRepos(String org) throws Exception {
		String url = baseUrl + "/orgs/" + org + "/repos?per_page=100&type=all";
		HttpResponse<String> resp = send(url);
		if (resp.statusCode() == 404) {
			// Not an org — personal account. /users/{name}/repos only
			// returns public repos; /user/repos returns everything
			// (public + private + archived) for the authenticated user.
			url = baseUrl + "/user/repos?per_page=100&type=owner";
			resp = send(url);
		}
		if (resp.statusCode() != 200) {
			throw new RuntimeException(
					"HTTP " + resp.statusCode() + " listing repos for " + org
							+ ": " + resp.body()
			);
		}
		return collectPaginatedArrayItems(resp, null).stream()
				.map(
						node -> new RepoSummary(
								requireText(node, "name"),
								requireBoolean(node, "archived"),
								requireText(node, "visibility")
						)
				)
				.toList();
	}

	public RepoDetails getRepo(String org, String repo) throws Exception {
		JsonNode node = mapper
				.readTree(send(baseUrl + "/repos/" + org + "/" + repo).body());
		JsonNode sa = node.path("security_and_analysis");
		boolean secretScanning = false;
		boolean secretScanningPush = false;
		if (!sa.isMissingNode() && !sa.isNull()) {
			secretScanning = "enabled"
					.equals(requireText(sa.path("secret_scanning"), "status"));
			secretScanningPush = "enabled".equals(
					requireText(
							sa.path("secret_scanning_push_protection"),
							"status"
					)
			);
		}
		return new RepoDetails(
				requireText(node, "description"),
				requireText(node, "homepage"),
				requireBoolean(node, "has_issues"),
				requireBoolean(node, "has_projects"),
				requireBoolean(node, "has_wiki"),
				requireText(node, "default_branch"),
				requireBoolean(node, "allow_merge_commit"),
				requireBoolean(node, "allow_squash_merge"),
				requireBoolean(node, "allow_auto_merge"),
				requireBoolean(node, "delete_branch_on_merge"),
				secretScanning,
				secretScanningPush
		);
	}

	public boolean getVulnerabilityAlerts(String owner, String repo)
			throws Exception {
		HttpResponse<String> resp = send(
				baseUrl + "/repos/" + owner + "/" + repo
						+ "/vulnerability-alerts"
		);
		if (resp.statusCode() == 204) {
			return true;
		}
		if (resp.statusCode() == 404) {
			return false;
		}
		throw new RuntimeException(
				"Unexpected HTTP " + resp.statusCode()
						+ " for vulnerability-alerts on " + repo
		);
	}

	public boolean getAutomatedSecurityFixes(String org, String repo)
			throws Exception {
		HttpResponse<String> resp = send(
				baseUrl + "/repos/" + org + "/" + repo
						+ "/automated-security-fixes"
		);
		if (resp.statusCode() == 200) {
			return requireBoolean(mapper.readTree(resp.body()), "enabled");
		}
		if (resp.statusCode() == 404) {
			return false;
		}
		throw new RuntimeException(
				"Unexpected HTTP " + resp.statusCode()
						+ " for automated-security-fixes on " + repo
		);
	}

	public boolean getImmutableReleases(String owner, String repo)
			throws Exception {
		HttpResponse<String> resp = send(
				baseUrl + "/repos/" + owner + "/" + repo + "/immutable-releases"
		);
		if (resp.statusCode() == 200) {
			return requireBoolean(mapper.readTree(resp.body()), "enabled");
		}
		if (resp.statusCode() == 404) {
			return false;
		}
		throw new RuntimeException(
				"Unexpected HTTP " + resp.statusCode()
						+ " for immutable-releases on " + repo
		);
	}

	public Optional<BranchProtection> getBranchProtection(
			String owner,
			String repo,
			String branch
	) throws Exception {
		HttpResponse<String> resp = send(
				baseUrl + "/repos/" + owner + "/" + repo + "/branches/" + branch
						+ "/protection"
		);
		if (resp.statusCode() == 404) {
			return Optional.empty();
		}
		if (resp.statusCode() != 200) {
			throw new RuntimeException(
					"Unexpected HTTP " + resp.statusCode()
							+ " for branch protection on " + repo
			);
		}
		JsonNode node = mapper.readTree(resp.body());
		boolean enforceAdmins = requireBoolean(
				node.path("enforce_admins"),
				"enabled"
		);
		boolean linearHistory = requireBoolean(
				node.path("required_linear_history"),
				"enabled"
		);
		boolean allowForcePushes = requireBoolean(
				node.path("allow_force_pushes"),
				"enabled"
		);
		JsonNode rsc = node.path("required_status_checks");
		boolean strict = false;
		List<String> contexts = List.of();
		if (!rsc.isMissingNode() && !rsc.isNull()) {
			strict = requireBoolean(rsc, "strict");
			// Modern API returns checks[].context; legacy returns contexts[]
			JsonNode checks = rsc.path("checks");
			if (!checks.isMissingNode() && checks.isArray()
					&& checks.size() > 0) {
				contexts = StreamSupport.stream(checks.spliterator(), false)
						.map(c -> requireText(c, "context"))
						.toList();
			} else {
				JsonNode ctxArray = rsc.path("contexts");
				contexts = StreamSupport.stream(ctxArray.spliterator(), false)
						.map(JsonNode::asText)
						.toList();
			}
		}
		return Optional.of(
				new BranchProtection(
						enforceAdmins,
						linearHistory,
						allowForcePushes,
						strict,
						contexts
				)
		);
	}

	public List<String> getActionSecretNames(String org, String repo)
			throws Exception {
		String url = baseUrl + "/repos/" + org + "/" + repo
				+ "/actions/secrets?per_page=100";
		HttpResponse<String> resp = send(url);
		if (resp.statusCode() != 200) {
			throw new RuntimeException(
					"HTTP " + resp.statusCode() + " for action secrets on "
							+ repo + ": " + resp.body()
			);
		}
		return collectPaginatedArrayItems(resp, "secrets").stream()
				.map(s -> requireText(s, "name"))
				.toList();
	}

	public List<String> getEnvironmentNames(String org, String repo)
			throws Exception {
		String url = baseUrl + "/repos/" + org + "/" + repo
				+ "/environments?per_page=100";
		HttpResponse<String> resp = send(url);
		if (resp.statusCode() != 200) {
			throw new RuntimeException(
					"HTTP " + resp.statusCode() + " for environments on " + repo
							+ ": " + resp.body()
			);
		}
		return collectPaginatedArrayItems(resp, "environments").stream()
				.map(e -> requireText(e, "name"))
				.toList();
	}

	public List<String> getEnvironmentSecretNames(
			String org,
			String repo,
			String env
	) throws Exception {
		String url = baseUrl + "/repos/" + org + "/" + repo + "/environments/"
				+ env + "/secrets?per_page=100";
		HttpResponse<String> resp = send(url);
		if (resp.statusCode() != 200) {
			throw new RuntimeException(
					"HTTP " + resp.statusCode() + " for environment secrets on "
							+ repo + "/" + env + ": " + resp.body()
			);
		}
		return collectPaginatedArrayItems(resp, "secrets").stream()
				.map(s -> requireText(s, "name"))
				.toList();
	}

	public WorkflowPermissions getWorkflowPermissions(String org, String repo)
			throws Exception {
		HttpResponse<String> resp = send(
				baseUrl + "/repos/" + org + "/" + repo
						+ "/actions/permissions/workflow"
		);
		if (resp.statusCode() == 403) {
			throw new RuntimeException(
					"HTTP 403 for workflow permissions on " + repo
							+ " — token may lack admin scope"
			);
		}
		if (resp.statusCode() != 200) {
			throw new RuntimeException(
					"Unexpected HTTP " + resp.statusCode()
							+ " for workflow permissions on " + repo
			);
		}
		JsonNode node = mapper.readTree(resp.body());
		return new WorkflowPermissions(
				requireText(node, "default_workflow_permissions"),
				requireBoolean(node, "can_approve_pull_request_reviews")
		);
	}

	public Optional<Pages> getPages(String owner, String repo)
			throws Exception {
		HttpResponse<String> resp = send(
				baseUrl + "/repos/" + owner + "/" + repo + "/pages"
		);
		if (resp.statusCode() == 403) {
			throw new RuntimeException(
					"HTTP 403 for workflow permissions on " + repo
							+ " — token may lack admin scope"
			);
		}
		if (resp.statusCode() == 404) {
			return Optional.empty();
		}
		if (resp.statusCode() != 200) {
			throw new RuntimeException(
					"Unexpected HTTP " + resp.statusCode()
							+ " for workflow permissions on " + repo
			);
		}
		JsonNode node = mapper.readTree(resp.body());
		return Optional.of(
				new Pages(
						Pages.BuildType
								.valueOfRaw(node.path("build_type").asText())
				)
		);
	}

	/**
	 * Collects all items from a paginated API response, following Link headers.
	 * The caller is responsible for validating the status of {@code firstResp}
	 * before calling this method. {@code arrayField} names the JSON field that
	 * holds the array on each page; pass {@code null} when the page body is
	 * itself the array (e.g. the repos list endpoint).
	 */
	private List<JsonNode> collectPaginatedArrayItems(
			HttpResponse<String> firstResp,
			String arrayField
	) throws Exception {
		List<JsonNode> items = new ArrayList<>();
		HttpResponse<String> resp = firstResp;
		while (true) {
			JsonNode page = mapper.readTree(resp.body());
			Iterable<JsonNode> array = arrayField != null
					? page.path(arrayField)
					: page;
			for (JsonNode item : array) {
				items.add(item);
			}
			String next = extractNextLink(
					resp.headers().firstValue("Link").orElse("")
			);
			if (next == null) {
				break;
			}
			resp = send(next);
			if (resp.statusCode() != 200) {
				throw new RuntimeException(
						"HTTP " + resp.statusCode() + " fetching next page: "
								+ resp.body()
				);
			}
		}
		return items;
	}

	private static boolean requireBoolean(JsonNode node, String field) {
		JsonNode n = node.path(field);
		if (n.isMissingNode()) {
			throw new RuntimeException(
					"Expected field '" + field
							+ "' to be present in response but was missing"
			);
		}
		if (!n.isBoolean()) {
			throw new RuntimeException(
					"Expected field '" + field + "' to be a boolean but was: "
							+ n
			);
		}
		return n.booleanValue();
	}

	private static String requireText(JsonNode node, String field) {
		JsonNode n = node.path(field);
		if (n.isMissingNode()) {
			throw new RuntimeException(
					"Expected field '" + field
							+ "' to be present in response but was missing"
			);
		}
		if (n.isNull()) {
			// GitHub treats null and empty string as equivalent for optional
			// text fields (description, homepage, etc.)
			return "";
		}
		if (!n.isTextual()) {
			throw new RuntimeException(
					"Expected field '" + field + "' to be a string but was: "
							+ n
			);
		}
		return n.textValue();
	}

	private HttpResponse<String> send(String url) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.header("Authorization", "Bearer " + token)
				.header("Accept", "application/vnd.github+json")
				.header("X-GitHub-Api-Version", "2026-03-10")
				.GET()
				.build();
		HttpResponse<String> resp = http
				.send(request, HttpResponse.BodyHandlers.ofString());
		handleRateLimit(resp);
		return resp;
	}

	private void handleRateLimit(HttpResponse<String> resp)
			throws InterruptedException {
		String remaining = resp.headers()
				.firstValue("X-RateLimit-Remaining")
				.orElse("1000");
		if ("0".equals(remaining)) {
			long resetEpoch = Long.parseLong(
					resp.headers().firstValue("X-RateLimit-Reset").orElse("0")
			);
			long sleepMs = (resetEpoch * 1000L) - System.currentTimeMillis()
					+ 1000L;
			if (sleepMs > 0) {
				System.err.printf(
						"Rate limit reached. Sleeping %.1f seconds until reset...%n",
						sleepMs / 1000.0
				);
				Thread.sleep(sleepMs);
			}
		}
	}

	private static String extractNextLink(String linkHeader) {
		if (linkHeader == null || linkHeader.isBlank()) {
			return null;
		}
		for (String part : linkHeader.split(",")) {
			String[] segments = part.trim().split(";");
			if (segments.length == 2
					&& segments[1].trim().equals("rel=\"next\"")) {
				return segments[0].trim().replaceAll("[<>]", "");
			}
		}
		return null;
	}

}
