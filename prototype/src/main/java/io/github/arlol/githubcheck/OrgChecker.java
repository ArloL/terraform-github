package io.github.arlol.githubcheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import io.github.arlol.githubcheck.client.BranchProtection;
import io.github.arlol.githubcheck.client.GitHubClient;
import io.github.arlol.githubcheck.client.RepositoryMinimal;
import io.github.arlol.githubcheck.client.SecurityAndAnalysis;
import io.github.arlol.githubcheck.client.WorkflowPermissions;
import io.github.arlol.githubcheck.config.RepositoryArgs;

public class OrgChecker {

	static final List<String> BASE_STATUS_CHECKS = List.of(
			"check-actions.required-status-check",
			"codeql-analysis.required-status-check",
			"CodeQL",
			"zizmor"
	);

	private final GitHubClient client;
	private final String org;
	private final boolean fix;

	public OrgChecker(String token, String org) {
		this(new GitHubClient(token), org, false);
	}

	public OrgChecker(String token, String org, boolean fix) {
		this(new GitHubClient(token), org, fix);
	}

	OrgChecker(GitHubClient client, String org) {
		this(client, org, false);
	}

	OrgChecker(GitHubClient client, String org, boolean fix) {
		this.client = client;
		this.org = org;
		this.fix = fix;
	}

	public CheckResult check(List<RepositoryArgs> repositories)
			throws Exception {
		System.out.println("Fetching repo list for org: " + org);
		List<RepositoryMinimal> summaries = client.listOrgRepos(org);
		System.out.printf(
				"Found %d repos. Fetching details in parallel...%n",
				summaries.size()
		);

		long startFetch = System.currentTimeMillis();

		Map<String, RepositoryArgs> desiredByName = repositories.stream()
				.collect(Collectors.toMap(RepositoryArgs::name, r -> r));

		List<CheckResult.RepoCheckResult> results = new ArrayList<>();

		try (ExecutorService executor = Executors
				.newVirtualThreadPerTaskExecutor()) {
			List<Future<CheckResult.RepoCheckResult>> futures = summaries
					.stream()
					.map(
							summary -> executor.submit(
									() -> checkOne(summary, desiredByName)
							)
					)
					.toList();
			for (Future<CheckResult.RepoCheckResult> f : futures) {
				results.add(f.get());
			}
		}

		// Repos declared in config but not found in the org
		Set<String> foundNames = summaries.stream()
				.map(RepositoryMinimal::name)
				.collect(Collectors.toSet());
		repositories.stream()
				.filter(r -> !foundNames.contains(r.name()))
				.map(r -> CheckResult.RepoCheckResult.missing(r.name()))
				.forEach(results::add);

		double fetchSeconds = (System.currentTimeMillis() - startFetch)
				/ 1000.0;
		System.out.printf("Fetch complete in %.2f seconds%n%n", fetchSeconds);

		return new CheckResult(Collections.unmodifiableList(results));
	}

	private CheckResult.RepoCheckResult checkOne(
			RepositoryMinimal summary,
			Map<String, RepositoryArgs> desiredByName
	) {
		String name = summary.name();
		RepositoryArgs desired = desiredByName.get(name);
		if (desired == null) {
			return CheckResult.RepoCheckResult.unknown(name);
		}
		try {
			RepositoryState state = fetchState(summary);
			List<String> diffs = computeDiffs(state, desired);
			if (fix) {
				diffs = applyFixes(name, desired, diffs);
			}
			return diffs.isEmpty() ? CheckResult.RepoCheckResult.ok(name)
					: CheckResult.RepoCheckResult.drift(name, diffs);
		} catch (Exception e) {
			return CheckResult.RepoCheckResult.error(name, e.getMessage());
		}
	}

	RepositoryState fetchState(RepositoryMinimal summary) throws Exception {
		String name = summary.name();
		boolean archived = summary.archived();

		var details = client.getRepo(org, name);

		boolean vulnAlerts = false;
		boolean automatedSecurityFixes = false;
		if (!archived) {
			vulnAlerts = client.getVulnerabilityAlerts(org, name);
			automatedSecurityFixes = client
					.getAutomatedSecurityFixes(org, name);
		}

		var sa = details.securityAndAnalysis();
		boolean secretScanning = SecurityAndAnalysis.StatusObject.Status.ENABLED
				.equals(sa.secretScanning().status());
		boolean secretScanningPush = SecurityAndAnalysis.StatusObject.Status.ENABLED
				.equals(sa.secretScanningPushProtection().status());

		boolean protectionExists = false;
		boolean enforceAdmins = false;
		boolean linearHistory = false;
		boolean allowForcePushes = false;
		boolean strict = false;
		List<String> statusContexts = List.of();
		if (!archived && "public".equals(summary.visibility())) {
			var protection = client.getBranchProtection(org, name, "main");
			if (protection.isPresent()) {
				var bp = protection.orElseThrow();
				protectionExists = true;
				enforceAdmins = bp.enforceAdmins().enabled();
				linearHistory = bp.requiredLinearHistory().enabled();
				allowForcePushes = bp.allowForcePushes().enabled();
				var rsc = bp.requiredStatusChecks();
				if (rsc != null) {
					strict = rsc.strict();
					var checks = rsc.checks();
					if (checks != null && !checks.isEmpty()) {
						statusContexts = checks.stream()
								.map(
										BranchProtection.RequiredStatusChecks.StatusCheck::context
								)
								.toList();
					} else {
						var contexts = rsc.contexts();
						statusContexts = contexts != null ? contexts
								: List.of();
					}
				}
			}
		}

		List<String> secretNames = client.getActionSecretNames(org, name);
		List<String> envNames = client.getEnvironmentNames(org, name);

		Map<String, List<String>> envSecrets = new LinkedHashMap<>();
		for (String envName : envNames) {
			envSecrets.put(
					envName,
					client.getEnvironmentSecretNames(org, name, envName)
			);
		}

		WorkflowPermissions wfPerms = client.getWorkflowPermissions(org, name);

		return new RepositoryState(
				name,
				archived,
				summary.visibility(),
				details.description(),
				details.homepage(),
				details.hasIssues(),
				details.hasProjects(),
				details.hasWiki(),
				details.defaultBranch(),
				details.topics() != null ? details.topics() : List.of(),
				details.allowMergeCommit(),
				details.allowSquashMerge(),
				details.allowAutoMerge(),
				details.deleteBranchOnMerge(),
				vulnAlerts,
				automatedSecurityFixes,
				secretScanning,
				secretScanningPush,
				protectionExists,
				enforceAdmins,
				linearHistory,
				allowForcePushes,
				strict,
				statusContexts,
				secretNames,
				envSecrets,
				wfPerms
		);
	}

	List<String> computeDiffs(RepositoryState actual, RepositoryArgs desired) {
		List<String> diffs = new ArrayList<>();

		if (desired.archived()) {
			if (actual.archived()) {
				return List.of();
			} else {
				return List.of("archived");
			}
		} else {
			check(diffs, "archived", desired.archived(), actual.archived());
		}

		boolean isPublic = "public".equals(actual.visibility());

		check(
				diffs,
				"description",
				desired.description(),
				actual.description()
		);
		check(
				diffs,
				"homepage_url",
				desired.homepageUrl(),
				actual.homepageUrl()
		);
		check(diffs, "has_issues", true, actual.hasIssues());
		check(diffs, "has_projects", true, actual.hasProjects());
		check(diffs, "has_wiki", true, actual.hasWiki());
		check(diffs, "default_branch", "main", actual.defaultBranch());
		checkSets(
				diffs,
				"topics",
				new HashSet<>(desired.topics()),
				new HashSet<>(actual.topics())
		);
		check(diffs, "allow_merge_commit", false, actual.allowMergeCommit());
		check(diffs, "allow_squash_merge", false, actual.allowSquashMerge());
		check(diffs, "allow_auto_merge", true, actual.allowAutoMerge());
		check(
				diffs,
				"delete_branch_on_merge",
				true,
				actual.deleteBranchOnMerge()
		);

		check(
				diffs,
				"vulnerability_alerts",
				true,
				actual.vulnerabilityAlerts()
		);
		check(
				diffs,
				"automated_security_fixes",
				true,
				actual.automatedSecurityFixes()
		);
		check(diffs, "secret_scanning", true, actual.secretScanning());
		check(
				diffs,
				"secret_scanning_push_protection",
				true,
				actual.secretScanningPushProtection()
		);
		check(
				diffs,
				"workflow_permissions.default",
				WorkflowPermissions.DefaultWorkflowPermissions.READ,
				actual.workflowPermissions().defaultWorkflowPermissions()
		);
		check(
				diffs,
				"workflow_permissions.can_approve_prs",
				true,
				actual.workflowPermissions().canApprovePullRequestReviews()
		);

		if (isPublic) {
			if (!actual.branchProtectionExists()) {
				diffs.add("branch_protection: missing");
			} else {
				check(
						diffs,
						"branch_protection.enforce_admins",
						true,
						actual.enforceAdmins()
				);
				check(
						diffs,
						"branch_protection.required_linear_history",
						true,
						actual.requiredLinearHistory()
				);
				check(
						diffs,
						"branch_protection.allow_force_pushes",
						false,
						actual.allowForcePushes()
				);
				check(
						diffs,
						"branch_protection.required_status_checks.strict",
						false,
						actual.requiredStatusChecksStrict()
				);

				Set<String> wantContexts = new HashSet<>(BASE_STATUS_CHECKS);
				wantContexts.addAll(desired.requiredStatusChecks());
				Set<String> gotContexts = new HashSet<>(
						actual.requiredStatusCheckContexts()
				);
				checkSets(
						diffs,
						"branch_protection.required_status_checks",
						wantContexts,
						gotContexts
				);
			}
		}

		checkSets(
				diffs,
				"action_secrets",
				new HashSet<>(desired.actionsSecrets()),
				new HashSet<>(actual.actionSecretNames())
		);

		// Environments: check names
		Set<String> wantEnvs = new LinkedHashSet<>(
				desired.environments().keySet()
		);
		if (desired.pages()) {
			wantEnvs.add("github-pages");
		}
		Set<String> gotEnvs = actual.environmentSecretNames().keySet();
		checkSets(diffs, "environments", wantEnvs, gotEnvs);

		// Environment secrets: for each desired env that exists, check secrets
		for (var entry : desired.environments().entrySet()) {
			String envName = entry.getKey();
			List<String> wantSecrets = entry.getValue().secrets();
			List<String> gotSecrets = actual.environmentSecretNames()
					.getOrDefault(envName, List.of());
			checkSets(
					diffs,
					"environment." + envName + ".secrets",
					new HashSet<>(wantSecrets),
					new HashSet<>(gotSecrets)
			);
		}

		return diffs;
	}

	List<String> applyFixes(
			String name,
			RepositoryArgs desired,
			List<String> diffs
	) throws Exception {
		List<String> remaining = new ArrayList<>(diffs);
		Map<String, Object> fields = new LinkedHashMap<>();

		if (remaining.removeIf(d -> d.startsWith("description:"))) {
			fields.put("description", desired.description());
		}
		if (remaining.removeIf(d -> d.startsWith("homepage_url:"))) {
			fields.put("homepage", desired.homepageUrl());
		}
		if (remaining.removeIf(d -> d.startsWith("has_issues:"))) {
			fields.put("has_issues", true);
		}
		if (remaining.removeIf(d -> d.startsWith("has_projects:"))) {
			fields.put("has_projects", true);
		}
		if (remaining.removeIf(d -> d.startsWith("has_wiki:"))) {
			fields.put("has_wiki", true);
		}
		if (remaining.removeIf(d -> d.startsWith("allow_merge_commit:"))) {
			fields.put("allow_merge_commit", false);
		}
		if (remaining.removeIf(d -> d.startsWith("allow_squash_merge:"))) {
			fields.put("allow_squash_merge", false);
		}
		if (remaining.removeIf(d -> d.startsWith("allow_auto_merge:"))) {
			fields.put("allow_auto_merge", true);
		}
		if (remaining.removeIf(d -> d.startsWith("delete_branch_on_merge:"))) {
			fields.put("delete_branch_on_merge", true);
		}
		if (remaining.removeIf(d -> d.startsWith("archived:"))) {
			fields.put("archived", desired.archived());
		}

		if (!fields.isEmpty()) {
			client.updateRepository(org, name, fields);
			for (String field : fields.keySet()) {
				System.out.printf("[FIXED]   %s: %s updated%n", name, field);
			}
		}

		if (remaining.removeIf(d -> d.startsWith("topics "))) {
			client.replaceTopics(org, name, desired.topics());
			System.out.printf("[FIXED]   %s: topics updated%n", name);
		}

		return remaining;
	}

	public void printReport(CheckResult result) {
		List<CheckResult.RepoCheckResult> sorted = result.repos()
				.stream()
				.sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
				.toList();

		for (CheckResult.RepoCheckResult r : sorted) {
			switch (r.status()) {
			case OK -> System.out.printf("[OK]      %s%n", r.name());
			case DRIFT -> {
				System.out.printf("[DRIFT]   %s:%n", r.name());
				r.diffs()
						.forEach(d -> System.out.printf("            %s%n", d));
			}
			case ERROR ->
				System.out.printf("[ERROR]   %s: %s%n", r.name(), r.error());
			case UNKNOWN -> System.out
					.printf("[UNKNOWN] %s: not in desired config%n", r.name());
			case MISSING -> System.out.printf(
					"[MISSING] %s: in config but not found in org%n",
					r.name()
			);
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

	private static void check(
			List<String> diffs,
			String field,
			Object want,
			Object got
	) {
		if (!Objects.equals(want, got)) {
			diffs.add(field + ": want=" + want + " got=" + got);
		}
	}

	private static void checkSets(
			List<String> diffs,
			String field,
			Set<String> want,
			Set<String> got
	) {
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
