package io.github.arlol.githubcheck.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public class GitHubClient {

	// ─── Response records ────────────────────────────────────────────────────
	// Each record maps directly to its GitHub API response shape. Field names
	// follow Java convention; the SNAKE_CASE ObjectMapper strategy maps
	// e.g. has_issues → hasIssues automatically. Use @JsonProperty only when
	// the API name cannot be derived from the Java name (e.g. "private").
	// Nested objects use nested records. Optional absent fields will be null
	// when absent from the API response.

	public record RepoSummary(
			String name,
			boolean archived,
			String visibility
	) {
	}

	public record RepoDetails(
			long id,
			String nodeId,
			String name,
			String fullName,
			@JsonProperty("private") boolean isPrivate,
			boolean fork,
			boolean archived,
			boolean disabled,
			boolean isTemplate,
			String visibility,
			String defaultBranch,
			String description, // nullable — GitHub returns null for unset
			String homepage, // nullable — GitHub returns null for unset
			List<String> topics,
			boolean hasIssues,
			boolean hasProjects,
			boolean hasWiki,
			boolean hasDiscussions,
			boolean hasPages,
			boolean allowForking,
			boolean webCommitSignoffRequired,
			boolean allowSquashMerge,
			boolean allowMergeCommit,
			boolean allowRebaseMerge,
			boolean allowAutoMerge,
			boolean deleteBranchOnMerge,
			boolean allowUpdateBranch,
			String squashMergeCommitTitle,
			String squashMergeCommitMessage,
			String mergeCommitTitle,
			String mergeCommitMessage,
			// May be absent for archived repos or repos where security features
			// are not available (e.g. private repos without GHAS).
			@JsonProperty(
					required = false
			) SecurityAndAnalysis securityAndAnalysis
	) {

		public record SecurityAndAnalysis(
				StatusObject secretScanning,
				StatusObject secretScanningPushProtection,
				StatusObject advancedSecurity,
				StatusObject dependabotSecurityUpdates
		) {

			public record StatusObject(
					String status
			) {
			}

		}

	}

	public record BranchProtection(
			EnabledObject enforceAdmins,
			EnabledObject requiredLinearHistory,
			EnabledObject allowForcePushes,
			// Absent when no status-check rules are configured.
			@JsonProperty(
					required = false
			) RequiredStatusChecks requiredStatusChecks
	) {

		public record EnabledObject(
				boolean enabled
		) {
		}

		public record RequiredStatusChecks(
				boolean strict,
				// Modern API returns checks[].context; legacy returns
				// contexts[].
				List<StatusCheck> checks,
				List<String> contexts
		) {

			public record StatusCheck(
					String context
			) {
			}

		}

	}

	public record WorkflowPermissions(
			String defaultWorkflowPermissions,
			boolean canApprovePullRequestReviews
	) {
	}

	public record Pages(
			String status,
			// Absent in legacy Pages responses that predate the build_type
			// field.
			BuildType buildType
	) {

		public enum BuildType {

			WORKFLOW, LEGACY;

			@JsonCreator
			public static BuildType fromValue(String value) {
				return valueOf(value.toUpperCase());
			}

		}

	}

	// ─── Private deserialization helpers ─────────────────────────────────────

	private record EnabledResponse(
			boolean enabled
	) {
	}

	private record NamedItem(
			String name
	) {
	}

	// ─── Client ──────────────────────────────────────────────────────────────

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
		this.mapper = new ObjectMapper()
				.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
				.configure(
						DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
						false
				)
				.configure(
						DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
						true
				);
	}

	// ─── Public API ──────────────────────────────────────────────────────────

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
				.map(node -> mapper.convertValue(node, RepoSummary.class))
				.toList();
	}

	public RepoDetails getRepo(String org, String repo) throws Exception {
		HttpResponse<String> resp = send(
				baseUrl + "/repos/" + org + "/" + repo
		);
		if (resp.statusCode() != 200) {
			throw new RuntimeException(
					"HTTP " + resp.statusCode() + " fetching repo " + org + "/"
							+ repo + ": " + resp.body()
			);
		}
		return mapper.readValue(resp.body(), RepoDetails.class);
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
			return mapper.readValue(resp.body(), EnabledResponse.class)
					.enabled();
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
			return mapper.readValue(resp.body(), EnabledResponse.class)
					.enabled();
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
		return Optional
				.of(mapper.readValue(resp.body(), BranchProtection.class));
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
				.map(s -> mapper.convertValue(s, NamedItem.class).name())
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
				.map(e -> mapper.convertValue(e, NamedItem.class).name())
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
				.map(s -> mapper.convertValue(s, NamedItem.class).name())
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
		return mapper.readValue(resp.body(), WorkflowPermissions.class);
	}

	public Optional<Pages> getPages(String owner, String repo)
			throws Exception {
		HttpResponse<String> resp = send(
				baseUrl + "/repos/" + owner + "/" + repo + "/pages"
		);
		if (resp.statusCode() == 403) {
			throw new RuntimeException(
					"HTTP 403 for pages on " + repo
							+ " — token may lack admin scope"
			);
		}
		if (resp.statusCode() == 404) {
			return Optional.empty();
		}
		if (resp.statusCode() != 200) {
			throw new RuntimeException(
					"Unexpected HTTP " + resp.statusCode() + " for pages on "
							+ repo
			);
		}
		return Optional.of(mapper.readValue(resp.body(), Pages.class));
	}

	// ─── Pagination ──────────────────────────────────────────────────────────

	/**
	 * Collects all items from a paginated API response, following Link headers.
	 * The caller is responsible for validating the status of {@code firstResp}.
	 * {@code arrayField} names the JSON field that holds the array on each
	 * page; pass {@code null} when the page body is itself the array.
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
