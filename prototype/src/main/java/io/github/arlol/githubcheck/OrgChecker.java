package io.github.arlol.githubcheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class OrgChecker {

    static final List<String> BASE_STATUS_CHECKS = List.of(
            "check-actions.required-status-check",
            "codeql-analysis.required-status-check",
            "CodeQL",
            "zizmor");

    private final GitHubClient client;
    private final String org;

    public OrgChecker(String token, String org) {
        this(new GitHubClient(token), org);
    }

    OrgChecker(GitHubClient client, String org) {
        this.client = client;
        this.org = org;
    }

    public CheckResult check(List<Repository> repositories) throws Exception {
        System.out.println("Fetching repo list for org: " + org);
        List<GitHubClient.RepoSummary> summaries = client.listOrgRepos(org);
        System.out.printf("Found %d repos. Fetching details in parallel...%n",
                summaries.size());

        long startFetch = System.currentTimeMillis();

        Map<String, Repository> desiredByName = repositories.stream()
                .collect(Collectors.toMap(Repository::name, r -> r));

        List<CheckResult.RepoCheckResult> results = new ArrayList<>();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<CheckResult.RepoCheckResult>> futures = summaries.stream()
                    .map(summary -> executor.submit(() -> checkOne(summary, desiredByName)))
                    .toList();
            for (Future<CheckResult.RepoCheckResult> f : futures) {
                results.add(f.get());
            }
        }

        // Repos declared in config but not found in the org
        Set<String> foundNames = summaries.stream()
                .map(GitHubClient.RepoSummary::name)
                .collect(Collectors.toSet());
        repositories.stream()
                .filter(r -> !foundNames.contains(r.name()))
                .map(r -> CheckResult.RepoCheckResult.missing(r.name()))
                .forEach(results::add);

        double fetchSeconds = (System.currentTimeMillis() - startFetch) / 1000.0;
        System.out.printf("Fetch complete in %.2f seconds%n%n", fetchSeconds);

        return new CheckResult(Collections.unmodifiableList(results));
    }

    private CheckResult.RepoCheckResult checkOne(
            GitHubClient.RepoSummary summary,
            Map<String, Repository> desiredByName) {
        String name = summary.name();
        Repository desired = desiredByName.get(name);
        if (desired == null) {
            return CheckResult.RepoCheckResult.unknown(name);
        }
        try {
            RepositoryState state = fetchState(summary);
            List<String> diffs = computeDiffs(state, desired.args());
            return diffs.isEmpty()
                    ? CheckResult.RepoCheckResult.ok(name)
                    : CheckResult.RepoCheckResult.drift(name, diffs);
        } catch (Exception e) {
            return CheckResult.RepoCheckResult.error(name, e.getMessage());
        }
    }

    RepositoryState fetchState(GitHubClient.RepoSummary summary) throws Exception {
        String name = summary.name();
        boolean archived = summary.archived();

        GitHubClient.RepoDetails details = client.getRepo(org, name);

        boolean vulnAlerts = false;
        if (!archived) {
            vulnAlerts = client.getVulnerabilityAlerts(org, name);
        }

        boolean protectionExists = false;
        boolean enforceAdmins = false;
        boolean linearHistory = false;
        List<String> statusContexts = List.of();
        if (!archived && "public".equals(summary.visibility())) {
            var protection = client.getBranchProtection(org, name);
            if (protection.isPresent()) {
                var bp = protection.get();
                protectionExists = true;
                enforceAdmins = bp.enforceAdmins();
                linearHistory = bp.requiredLinearHistory();
                statusContexts = bp.requiredStatusCheckContexts();
            }
        }

        List<String> secretNames = client.getActionSecretNames(org, name);
        List<String> envNames = client.getEnvironmentNames(org, name);

        Map<String, List<String>> envSecrets = new LinkedHashMap<>();
        for (String envName : envNames) {
            envSecrets.put(envName, client.getEnvironmentSecretNames(org, name, envName));
        }

        GitHubClient.WorkflowPermissions wfPerms = client.getWorkflowPermissions(org, name);

        return new RepositoryState(
                name,
                archived,
                summary.visibility(),
                details.allowMergeCommit(),
                details.allowSquashMerge(),
                details.allowAutoMerge(),
                details.deleteBranchOnMerge(),
                vulnAlerts,
                details.secretScanning(),
                details.secretScanningPushProtection(),
                protectionExists,
                enforceAdmins,
                linearHistory,
                statusContexts,
                secretNames,
                envSecrets,
                wfPerms.defaultPermissions(),
                wfPerms.canApprovePullRequestReviews());
    }

    List<String> computeDiffs(RepositoryState actual, RepositoryArgs desired) {
        List<String> diffs = new ArrayList<>();
        boolean archived = actual.archived();
        boolean isPublic = "public".equals(actual.visibility());

        check(diffs, "allow_merge_commit", false, actual.allowMergeCommit());
        check(diffs, "allow_squash_merge", false, actual.allowSquashMerge());
        check(diffs, "allow_auto_merge", true, actual.allowAutoMerge());
        check(diffs, "delete_branch_on_merge", true, actual.deleteBranchOnMerge());

        if (!archived) {
            check(diffs, "vulnerability_alerts", true, actual.vulnerabilityAlerts());
            check(diffs, "secret_scanning", true, actual.secretScanning());
            check(diffs, "secret_scanning_push_protection", true,
                    actual.secretScanningPushProtection());
            check(diffs, "workflow_permissions.default", "read",
                    actual.workflowPermissionsDefault());
            check(diffs, "workflow_permissions.can_approve_prs", true,
                    actual.canApprovePullRequestReviews());
        }

        if (!archived && isPublic) {
            if (!actual.branchProtectionExists()) {
                diffs.add("branch_protection: missing");
            } else {
                check(diffs, "branch_protection.enforce_admins", true,
                        actual.enforceAdmins());
                check(diffs, "branch_protection.required_linear_history", true,
                        actual.requiredLinearHistory());

                Set<String> wantContexts = new HashSet<>(BASE_STATUS_CHECKS);
                wantContexts.addAll(desired.requiredStatusChecks());
                Set<String> gotContexts = new HashSet<>(actual.requiredStatusCheckContexts());
                checkSets(diffs, "branch_protection.required_status_checks",
                        wantContexts, gotContexts);
            }
        }

        checkSets(diffs, "action_secrets",
                new HashSet<>(desired.actionSecrets()),
                new HashSet<>(actual.actionSecretNames()));

        // Environments: check names
        Set<String> wantEnvs = desired.environments().keySet();
        Set<String> gotEnvs = actual.environmentSecretNames().keySet();
        checkSets(diffs, "environments", wantEnvs, gotEnvs);

        // Environment secrets: for each desired env that exists, check secrets
        for (Map.Entry<String, EnvironmentArgs> entry : desired.environments().entrySet()) {
            String envName = entry.getKey();
            List<String> wantSecrets = entry.getValue().secrets();
            List<String> gotSecrets = actual.environmentSecretNames()
                    .getOrDefault(envName, List.of());
            checkSets(diffs, "environment." + envName + ".secrets",
                    new HashSet<>(wantSecrets), new HashSet<>(gotSecrets));
        }

        return diffs;
    }

    public void printReport(CheckResult result) {
        List<CheckResult.RepoCheckResult> sorted = result.repos().stream()
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .toList();

        for (CheckResult.RepoCheckResult r : sorted) {
            switch (r.status()) {
                case OK ->
                    System.out.printf("[OK]      %s%n", r.name());
                case DRIFT -> {
                    System.out.printf("[DRIFT]   %s:%n", r.name());
                    r.diffs().forEach(d -> System.out.printf("            %s%n", d));
                }
                case ERROR ->
                    System.out.printf("[ERROR]   %s: %s%n", r.name(), r.error());
                case UNKNOWN ->
                    System.out.printf("[UNKNOWN] %s: not in desired config%n", r.name());
                case MISSING ->
                    System.out.printf("[MISSING] %s: in config but not found in org%n",
                            r.name());
            }
        }

        System.out.println();
        System.out.println("=== Summary ===");
        System.out.printf("Repos checked:  %d%n", result.repos().size());
        System.out.printf("OK:             %d%n", result.okCount());
        System.out.printf("Drifted:        %d%n", result.driftCount());
        System.out.printf("Errored:        %d%n", result.errorCount());
        System.out.printf("Unknown:        %d%n", result.unknownCount());
    }

    private static void check(List<String> diffs, String field, Object want, Object got) {
        if (!Objects.equals(want, got)) {
            diffs.add(field + ": want=" + want + " got=" + got);
        }
    }

    private static void checkSets(List<String> diffs, String field,
            Set<String> want, Set<String> got) {
        Set<String> missing = new HashSet<>(want);
        missing.removeAll(got);
        Set<String> extra = new HashSet<>(got);
        extra.removeAll(want);
        if (!missing.isEmpty()) {
            diffs.add(field + " missing: " + sorted(missing));
        }
        if (!extra.isEmpty()) {
            diffs.add(field + " extra: " + sorted(extra));
        }
    }

    private static List<String> sorted(Set<String> s) {
        List<String> list = new ArrayList<>(s);
        Collections.sort(list);
        return list;
    }
}
