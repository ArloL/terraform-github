package io.github.arlol.githubcheck;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class GitHubClient {

    // Response records — the parsed shapes of GitHub API responses
    record RepoSummary(String name, boolean archived, String visibility) {}

    record RepoDetails(
            boolean allowMergeCommit,
            boolean allowSquashMerge,
            boolean allowAutoMerge,
            boolean deleteBranchOnMerge,
            boolean secretScanning,
            boolean secretScanningPushProtection) {}

    record BranchProtection(
            boolean enforceAdmins,
            boolean requiredLinearHistory,
            List<String> requiredStatusCheckContexts) {}

    record WorkflowPermissions(
            String defaultPermissions,
            boolean canApprovePullRequestReviews) {}

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
        List<RepoSummary> all = new ArrayList<>();
        String url = baseUrl + "/orgs/" + org + "/repos?per_page=100&type=all";
        while (url != null) {
            HttpResponse<String> resp = send(url);
            JsonNode page = mapper.readTree(resp.body());
            for (JsonNode node : page) {
                all.add(new RepoSummary(
                        node.path("name").asText(),
                        node.path("archived").asBoolean(false),
                        node.path("visibility").asText("public")));
            }
            url = extractNextLink(resp.headers().firstValue("Link").orElse(""));
        }
        return all;
    }

    public RepoDetails getRepo(String org, String repo) throws Exception {
        JsonNode node = mapper.readTree(
                send(baseUrl + "/repos/" + org + "/" + repo).body());
        JsonNode sa = node.path("security_and_analysis");
        boolean secretScanning = "enabled".equals(
                sa.path("secret_scanning").path("status").asText());
        boolean secretScanningPush = "enabled".equals(
                sa.path("secret_scanning_push_protection").path("status").asText());
        return new RepoDetails(
                node.path("allow_merge_commit").asBoolean(true),
                node.path("allow_squash_merge").asBoolean(true),
                node.path("allow_auto_merge").asBoolean(false),
                node.path("delete_branch_on_merge").asBoolean(false),
                secretScanning,
                secretScanningPush);
    }

    public boolean getVulnerabilityAlerts(String org, String repo) throws Exception {
        HttpResponse<String> resp = send(
                baseUrl + "/repos/" + org + "/" + repo + "/vulnerability-alerts");
        if (resp.statusCode() == 204) {
            return true;
        }
        if (resp.statusCode() == 404) {
            return false;
        }
        throw new RuntimeException("Unexpected HTTP " + resp.statusCode()
                + " for vulnerability-alerts on " + repo);
    }

    public Optional<BranchProtection> getBranchProtection(String org, String repo)
            throws Exception {
        HttpResponse<String> resp = send(
                baseUrl + "/repos/" + org + "/" + repo + "/branches/main/protection");
        if (resp.statusCode() == 404) {
            return Optional.empty();
        }
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Unexpected HTTP " + resp.statusCode()
                    + " for branch protection on " + repo);
        }
        JsonNode node = mapper.readTree(resp.body());
        boolean enforceAdmins = node.path("enforce_admins").path("enabled").asBoolean(false);
        boolean linearHistory = node.path("required_linear_history").path("enabled").asBoolean(false);
        JsonNode rsc = node.path("required_status_checks");
        List<String> contexts;
        // Modern API returns checks[].context; legacy returns contexts[]
        JsonNode checks = rsc.path("checks");
        if (!checks.isMissingNode() && checks.isArray() && checks.size() > 0) {
            contexts = StreamSupport.stream(checks.spliterator(), false)
                    .map(c -> c.path("context").asText())
                    .toList();
        } else {
            JsonNode ctxArray = rsc.path("contexts");
            contexts = StreamSupport.stream(ctxArray.spliterator(), false)
                    .map(JsonNode::asText)
                    .toList();
        }
        return Optional.of(new BranchProtection(enforceAdmins, linearHistory, contexts));
    }

    public List<String> getActionSecretNames(String org, String repo) throws Exception {
        JsonNode node = mapper.readTree(send(
                baseUrl + "/repos/" + org + "/" + repo + "/actions/secrets?per_page=100").body());
        return StreamSupport.stream(node.path("secrets").spliterator(), false)
                .map(s -> s.path("name").asText())
                .toList();
    }

    public List<String> getEnvironmentNames(String org, String repo) throws Exception {
        JsonNode node = mapper.readTree(send(
                baseUrl + "/repos/" + org + "/" + repo + "/environments").body());
        return StreamSupport.stream(node.path("environments").spliterator(), false)
                .map(e -> e.path("name").asText())
                .toList();
    }

    public List<String> getEnvironmentSecretNames(String org, String repo, String env)
            throws Exception {
        JsonNode node = mapper.readTree(send(
                baseUrl + "/repos/" + org + "/" + repo
                        + "/environments/" + env + "/secrets?per_page=100").body());
        return StreamSupport.stream(node.path("secrets").spliterator(), false)
                .map(s -> s.path("name").asText())
                .toList();
    }

    public WorkflowPermissions getWorkflowPermissions(String org, String repo)
            throws Exception {
        HttpResponse<String> resp = send(
                baseUrl + "/repos/" + org + "/" + repo + "/actions/permissions/workflow");
        if (resp.statusCode() == 403) {
            throw new RuntimeException("HTTP 403 for workflow permissions on " + repo
                    + " — token may lack admin scope");
        }
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Unexpected HTTP " + resp.statusCode()
                    + " for workflow permissions on " + repo);
        }
        JsonNode node = mapper.readTree(resp.body());
        return new WorkflowPermissions(
                node.path("default_workflow_permissions").asText("read"),
                node.path("can_approve_pull_request_reviews").asBoolean(false));
    }

    private HttpResponse<String> send(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .GET()
                .build();
        HttpResponse<String> resp = http.send(request,
                HttpResponse.BodyHandlers.ofString());
        handleRateLimit(resp);
        return resp;
    }

    private void handleRateLimit(HttpResponse<String> resp) throws InterruptedException {
        String remaining = resp.headers()
                .firstValue("X-RateLimit-Remaining").orElse("1000");
        if ("0".equals(remaining)) {
            long resetEpoch = Long.parseLong(
                    resp.headers().firstValue("X-RateLimit-Reset").orElse("0"));
            long sleepMs = (resetEpoch * 1000L) - System.currentTimeMillis() + 1000L;
            if (sleepMs > 0) {
                System.err.printf("Rate limit reached. Sleeping %.1f seconds until reset...%n",
                        sleepMs / 1000.0);
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
            if (segments.length == 2 && segments[1].trim().equals("rel=\"next\"")) {
                return segments[0].trim().replaceAll("[<>]", "");
            }
        }
        return null;
    }
}
