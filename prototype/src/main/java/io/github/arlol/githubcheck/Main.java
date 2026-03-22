package io.github.arlol.githubcheck;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Main {

    // --- Desired state (hardcoded from variables.tf) ---

    record DesiredRepo(
        String name,
        boolean archived,
        String visibility,
        List<String> extraStatusChecks,
        List<String> secretNames,
        List<String> environmentNames
    ) {}

    static final List<String> BASE_STATUS_CHECKS = List.of(
        "check-actions.required-status-check",
        "codeql-analysis.required-status-check",
        "CodeQL",
        "zizmor"
    );

    static List<DesiredRepo> buildDesired() {
        return List.of(
            repo("abenteuer-irland"),
            repo("actions", true),
            repo("advent-of-code"),
            repo("angular-playground", List.of("pr-check.required-status-check"), List.of(), List.of()),
            repo("arlol.github.io"),
            repo("beatunes-keytocomment"),
            repo("bulma-playground"),
            repo("business-english"),
            repo("calver-tag-action"),
            repo("chorito", List.of("main.required-status-check"), List.of("PAT"), List.of()),
            repo("corporate-python"),
            repo("dependabot-dockerfile-test"),
            repo("dotfiles"),
            repo("dotnet-http-client-reproduction", true),
            repo("eclipse-projects"),
            repo("effortful-retrieval-questions"),
            repo("git-dora-lead-time-calculator", List.of("main.required-status-check"), List.of(), List.of()),
            repo("git-presentation-2018-10"),
            repo("gitfx", true),
            repo("graalfx", true),
            repo("kafka-debugger"),
            repo("homebrew-tap"),
            repo("menubar-scripts"),
            repo("music-stuff", List.of("test.required-status-check"), List.of(), List.of()),
            repo("mvnx", List.of("main.required-status-check"), List.of(), List.of()),
            repo("myprojects-cleaner", List.of("main.required-status-check"), List.of(), List.of()),
            repo("newlinechecker", List.of("main.required-status-check"), List.of(), List.of()),
            repo("nope-amine"),
            repo("open-webui-runner"),
            repo("pico-playground", true),
            repo("postgres-sync-demo"),
            repo("python-nc"),
            repo("rss-to-mail", List.of("main.required-status-check"), List.of(), List.of()),
            repo("sci-fi-movies"),
            repo("self-hosted-gh-actions-runner", true),
            repo("terraform-github", List.of(), List.of(), List.of("production")),
            repo("testcontainers-colima-github-actions", true),
            repo("tsaf-parser", List.of("test.required-status-check"), List.of(), List.of()),
            repo("vagrant-ssh-config"),
            repo("vitest-link-reproduction", true),
            repo("wait-for-ports", List.of("main.required-status-check"), List.of(), List.of()),
            repo("webapp-classloader-test", List.of("main.required-status-check"), List.of(), List.of()),
            repo("website-janitor", List.of("main.required-status-check"), List.of(), List.of())
        );
    }

    static DesiredRepo repo(String name) {
        return new DesiredRepo(name, false, "public", List.of(), List.of(), List.of());
    }

    static DesiredRepo repo(String name, boolean archived) {
        return new DesiredRepo(name, archived, "public", List.of(), List.of(), List.of());
    }

    static DesiredRepo repo(String name, List<String> extraChecks, List<String> secrets, List<String> envs) {
        return new DesiredRepo(name, false, "public", extraChecks, secrets, envs);
    }

    // --- GitHub API client ---

    static class GitHubClient {
        private static final String BASE = "https://api.github.com";
        private final HttpClient http;
        private final ObjectMapper mapper;
        private final String token;

        GitHubClient(String token) {
            this.token = token;
            this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
            this.mapper = new ObjectMapper();
        }

        List<JsonNode> listOrgRepos(String org) throws Exception {
            List<JsonNode> all = new ArrayList<>();
            String url = BASE + "/orgs/" + org + "/repos?per_page=100&type=all";
            while (url != null) {
                HttpResponse<String> resp = send(url);
                JsonNode page = mapper.readTree(resp.body());
                page.forEach(all::add);
                url = extractNextLink(resp.headers().firstValue("Link").orElse(""));
            }
            return all;
        }

        JsonNode getRepo(String org, String repo) throws Exception {
            return mapper.readTree(send(BASE + "/repos/" + org + "/" + repo).body());
        }

        boolean getVulnerabilityAlerts(String org, String repo) throws Exception {
            HttpResponse<String> resp = send(BASE + "/repos/" + org + "/" + repo + "/vulnerability-alerts");
            if (resp.statusCode() == 204) return true;
            if (resp.statusCode() == 404) return false;
            throw new RuntimeException("Unexpected status " + resp.statusCode() + " for vulnerability-alerts on " + repo);
        }

        JsonNode getBranchProtection(String org, String repo) throws Exception {
            HttpResponse<String> resp = send(BASE + "/repos/" + org + "/" + repo + "/branches/main/protection");
            if (resp.statusCode() == 404) return null;
            if (resp.statusCode() != 200) throw new RuntimeException("HTTP " + resp.statusCode() + " for branch protection on " + repo);
            return mapper.readTree(resp.body());
        }

        JsonNode getActionsSecrets(String org, String repo) throws Exception {
            return mapper.readTree(send(BASE + "/repos/" + org + "/" + repo + "/actions/secrets?per_page=100").body());
        }

        JsonNode getEnvironments(String org, String repo) throws Exception {
            return mapper.readTree(send(BASE + "/repos/" + org + "/" + repo + "/environments").body());
        }

        JsonNode getWorkflowPermissions(String org, String repo) throws Exception {
            HttpResponse<String> resp = send(BASE + "/repos/" + org + "/" + repo + "/actions/permissions/workflow");
            if (resp.statusCode() == 403) throw new RuntimeException("HTTP 403 for workflow permissions on " + repo + " (insufficient token scope)");
            if (resp.statusCode() != 200) throw new RuntimeException("HTTP " + resp.statusCode() + " for workflow permissions on " + repo);
            return mapper.readTree(resp.body());
        }

        private HttpResponse<String> send(String url) throws Exception {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .GET()
                .build();
            return http.send(req, HttpResponse.BodyHandlers.ofString());
        }

        private static String extractNextLink(String linkHeader) {
            if (linkHeader == null || linkHeader.isBlank()) return null;
            for (String part : linkHeader.split(",")) {
                String[] segments = part.trim().split(";");
                if (segments.length == 2 && segments[1].trim().equals("rel=\"next\"")) {
                    return segments[0].trim().replaceAll("[<>]", "");
                }
            }
            return null;
        }
    }

    // --- Actual state record ---

    record RepoResult(
        String name,
        boolean archived,
        String visibility,
        boolean allowMergeCommit,
        boolean allowSquashMerge,
        boolean allowAutoMerge,
        boolean deleteBranchOnMerge,
        boolean vulnerabilityAlerts,
        boolean secretScanning,
        boolean secretScanningPushProtection,
        boolean branchProtectionExists,
        boolean enforceAdmins,
        boolean requiredLinearHistory,
        List<String> statusCheckContexts,
        List<String> secretNames,
        List<String> environmentNames,
        String workflowPermDefault,
        boolean canApprovePrs,
        String error
    ) {}

    // --- Fetch one repo's full state ---

    static RepoResult fetchOne(GitHubClient client, String org, JsonNode node) {
        String name = node.path("name").asText();
        boolean archived = node.path("archived").asBoolean();
        String visibility = node.path("visibility").asText("public");
        try {
            JsonNode repo = client.getRepo(org, name);

            boolean allowMergeCommit = repo.path("allow_merge_commit").asBoolean(true);
            boolean allowSquashMerge = repo.path("allow_squash_merge").asBoolean(true);
            boolean allowAutoMerge = repo.path("allow_auto_merge").asBoolean(false);
            boolean deleteBranchOnMerge = repo.path("delete_branch_on_merge").asBoolean(false);

            boolean vulnAlerts = false;
            boolean secretScanning = false;
            boolean secretScanningPush = false;
            if (!archived) {
                vulnAlerts = client.getVulnerabilityAlerts(org, name);
                JsonNode sa = repo.path("security_and_analysis");
                secretScanning = "enabled".equals(sa.path("secret_scanning").path("status").asText());
                secretScanningPush = "enabled".equals(sa.path("secret_scanning_push_protection").path("status").asText());
            }

            boolean protectionExists = false;
            boolean enforceAdmins = false;
            boolean linearHistory = false;
            List<String> statusChecks = List.of();
            if (!archived && "public".equals(visibility)) {
                JsonNode protection = client.getBranchProtection(org, name);
                if (protection != null) {
                    protectionExists = true;
                    enforceAdmins = protection.path("enforce_admins").path("enabled").asBoolean(false);
                    linearHistory = protection.path("required_linear_history").path("enabled").asBoolean(false);
                    JsonNode rsc = protection.path("required_status_checks");
                    if (!rsc.isMissingNode()) {
                        // Try modern "checks" array first, fall back to legacy "contexts"
                        JsonNode checks = rsc.path("checks");
                        if (!checks.isMissingNode() && checks.isArray() && checks.size() > 0) {
                            statusChecks = StreamSupport.stream(checks.spliterator(), false)
                                .map(c -> c.path("context").asText())
                                .collect(Collectors.toList());
                        } else {
                            JsonNode contexts = rsc.path("contexts");
                            statusChecks = StreamSupport.stream(contexts.spliterator(), false)
                                .map(JsonNode::asText)
                                .collect(Collectors.toList());
                        }
                    }
                }
            }

            JsonNode secretsNode = client.getActionsSecrets(org, name);
            List<String> secretNames = StreamSupport.stream(
                secretsNode.path("secrets").spliterator(), false)
                .map(s -> s.path("name").asText())
                .collect(Collectors.toList());

            JsonNode envsNode = client.getEnvironments(org, name);
            List<String> envNames = StreamSupport.stream(
                envsNode.path("environments").spliterator(), false)
                .map(e -> e.path("name").asText())
                .collect(Collectors.toList());

            JsonNode workflowPerms = client.getWorkflowPermissions(org, name);
            String wfDefault = workflowPerms.path("default_workflow_permissions").asText("read");
            boolean canApprovePrs = workflowPerms.path("can_approve_pull_request_reviews").asBoolean(false);

            return new RepoResult(name, archived, visibility,
                allowMergeCommit, allowSquashMerge, allowAutoMerge, deleteBranchOnMerge,
                vulnAlerts, secretScanning, secretScanningPush,
                protectionExists, enforceAdmins, linearHistory, statusChecks,
                secretNames, envNames,
                wfDefault, canApprovePrs,
                null);
        } catch (Exception e) {
            return new RepoResult(name, archived, visibility,
                false, false, false, false, false, false, false,
                false, false, false, List.of(), List.of(), List.of(),
                null, false,
                e.getMessage());
        }
    }

    // --- Diff reporting ---

    static List<String> computeDiffs(RepoResult actual, DesiredRepo desired) {
        List<String> diffs = new ArrayList<>();
        boolean isArchived = actual.archived();
        boolean isPublic = "public".equals(actual.visibility());

        check(diffs, "allow_merge_commit", false, actual.allowMergeCommit());
        check(diffs, "allow_squash_merge", false, actual.allowSquashMerge());
        check(diffs, "allow_auto_merge", true, actual.allowAutoMerge());
        check(diffs, "delete_branch_on_merge", true, actual.deleteBranchOnMerge());

        if (!isArchived) {
            check(diffs, "vulnerability_alerts", true, actual.vulnerabilityAlerts());
            check(diffs, "secret_scanning", true, actual.secretScanning());
            check(diffs, "secret_scanning_push_protection", true, actual.secretScanningPushProtection());
            check(diffs, "workflow_permissions.default", "read", actual.workflowPermDefault());
            check(diffs, "workflow_permissions.can_approve_prs", true, actual.canApprovePrs());
        }

        if (!isArchived && isPublic) {
            if (!actual.branchProtectionExists()) {
                diffs.add("branch_protection: missing entirely");
            } else {
                check(diffs, "branch_protection.enforce_admins", true, actual.enforceAdmins());
                check(diffs, "branch_protection.required_linear_history", true, actual.requiredLinearHistory());

                Set<String> wantChecks = new HashSet<>(BASE_STATUS_CHECKS);
                wantChecks.addAll(desired.extraStatusChecks());
                Set<String> gotChecks = new HashSet<>(actual.statusCheckContexts());

                Set<String> missing = new HashSet<>(wantChecks);
                missing.removeAll(gotChecks);
                Set<String> extra = new HashSet<>(gotChecks);
                extra.removeAll(wantChecks);

                if (!missing.isEmpty()) diffs.add("branch_protection.required_status_checks missing: " + sorted(missing));
                if (!extra.isEmpty())   diffs.add("branch_protection.required_status_checks extra: " + sorted(extra));
            }
        }

        checkSets(diffs, "actions_secrets", new HashSet<>(desired.secretNames()), new HashSet<>(actual.secretNames()));
        checkSets(diffs, "environments", new HashSet<>(desired.environmentNames()), new HashSet<>(actual.environmentNames()));

        return diffs;
    }

    static void check(List<String> diffs, String field, Object want, Object got) {
        if (!Objects.equals(want, got)) {
            diffs.add(field + ": want=" + want + " got=" + got);
        }
    }

    static void checkSets(List<String> diffs, String field, Set<String> want, Set<String> got) {
        Set<String> missing = new HashSet<>(want);
        missing.removeAll(got);
        Set<String> extra = new HashSet<>(got);
        extra.removeAll(want);
        if (!missing.isEmpty()) diffs.add(field + " missing: " + sorted(missing));
        if (!extra.isEmpty())   diffs.add(field + " extra: " + sorted(extra));
    }

    static List<String> sorted(Set<String> s) {
        List<String> l = new ArrayList<>(s);
        Collections.sort(l);
        return l;
    }

    static void printDiffReport(List<RepoResult> actual, List<DesiredRepo> desired) {
        Map<String, DesiredRepo> desiredMap = desired.stream()
            .collect(Collectors.toMap(DesiredRepo::name, d -> d));

        List<RepoResult> sorted = actual.stream()
            .sorted(Comparator.comparing(RepoResult::name))
            .toList();

        int driftCount = 0;
        int errorCount = 0;

        for (RepoResult repo : sorted) {
            if (repo.error() != null) {
                System.out.printf("[ERROR]   %s: %s%n", repo.name(), repo.error());
                errorCount++;
                continue;
            }
            DesiredRepo want = desiredMap.get(repo.name());
            if (want == null) {
                System.out.printf("[UNKNOWN] %s: not in desired config (variables.tf)%n", repo.name());
                continue;
            }
            List<String> diffs = computeDiffs(repo, want);
            if (diffs.isEmpty()) {
                System.out.printf("[OK]      %s%n", repo.name());
            } else {
                System.out.printf("[DRIFT]   %s:%n", repo.name());
                diffs.forEach(d -> System.out.printf("            %s%n", d));
                driftCount++;
            }
        }

        Set<String> actualNames = actual.stream().map(RepoResult::name).collect(Collectors.toSet());
        List<String> missing = desired.stream()
            .filter(d -> !actualNames.contains(d.name()))
            .map(DesiredRepo::name)
            .sorted()
            .toList();

        System.out.println();
        System.out.println("=== Summary ===");
        System.out.printf("Repos checked:  %d%n", actual.size());
        System.out.printf("Repos OK:       %d%n", actual.size() - driftCount - errorCount - (int) actual.stream().filter(r -> !desiredMap.containsKey(r.name()) && r.error() == null).count());
        System.out.printf("Repos drifted:  %d%n", driftCount);
        System.out.printf("Repos errored:  %d%n", errorCount);
        System.out.printf("Repos unknown:  %d%n", (int) actual.stream().filter(r -> !desiredMap.containsKey(r.name()) && r.error() == null).count());
        if (!missing.isEmpty()) {
            System.out.printf("Repos missing from org (in variables.tf but not found): %s%n", missing);
        }
    }

    // --- Main ---

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        String token = System.getenv("GITHUB_TOKEN");
        if (token == null || token.isBlank()) {
            System.err.println("ERROR: GITHUB_TOKEN environment variable not set");
            System.exit(1);
        }

        GitHubClient client = new GitHubClient(token);
        String org = "ArloL";

        System.out.println("Fetching repo list for org: " + org);
        List<JsonNode> repoList = client.listOrgRepos(org);
        System.out.printf("Found %d repos. Fetching details in parallel...%n", repoList.size());

        List<DesiredRepo> desired = buildDesired();

        List<RepoResult> results;
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<RepoResult>> futures = repoList.stream()
                .map(repoNode -> executor.submit(() -> fetchOne(client, org, repoNode)))
                .toList();

            results = new ArrayList<>();
            for (Future<RepoResult> f : futures) {
                results.add(f.get());
            }
        }

        long fetchTime = System.currentTimeMillis() - startTime;
        System.out.printf("Fetch complete in %.2f seconds%n%n", fetchTime / 1000.0);

        printDiffReport(results, desired);

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.printf("%nTotal execution time: %.2f seconds%n", totalTime / 1000.0);
    }
}
