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
				diffs = applyFixes(name, state, desired, diffs);
			}
			return diffs.isEmpty() ? CheckResult.RepoCheckResult.ok(name)
					: CheckResult.RepoCheckResult.drift(name, diffs);
		} catch (Exception e) {
			return CheckResult.RepoCheckResult.error(name, e.getMessage());
		}
	}

	// ─── Fetch
	// ──────────────────────────────────────────────────────────────

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

		BranchProtection branchProtection = null;
		if (!archived && "public".equals(summary.visibility())) {
			branchProtection = client.getBranchProtection(org, name, "main")
					.orElse(null);
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
				summary,
				details,
				vulnAlerts,
				automatedSecurityFixes,
				branchProtection,
				secretNames,
				envSecrets,
				wfPerms
		);
	}

	// ─── Diff
	// ──────────────────────────────────────────────────────────────

	List<String> computeDiffs(RepositoryState actual, RepositoryArgs desired) {
		List<String> diffs = new ArrayList<>();

		if (desired.archived()) {
			if (actual.summary().archived()) {
				return List.of();
			} else {
				return List.of("archived");
			}
		}

		checkRepoSettings(diffs, actual, desired);
		checkTopics(diffs, actual, desired);
		check(
				diffs,
				"default_branch",
				"main",
				actual.details().defaultBranch()
		);
		checkSecuritySettings(diffs, actual);
		checkWorkflowPermissions(diffs, actual);
		checkBranchProtection(diffs, actual, desired);
		checkSecrets(diffs, actual, desired);

		return diffs;
	}

	private void checkRepoSettings(
			List<String> diffs,
			RepositoryState actual,
			RepositoryArgs desired
	) {
		var details = actual.details();
		check(
				diffs,
				"archived",
				desired.archived(),
				actual.summary().archived()
		);
		check(
				diffs,
				"description",
				desired.description(),
				Objects.toString(details.description(), "")
		);
		check(
				diffs,
				"homepage_url",
				desired.homepageUrl(),
				Objects.toString(details.homepage(), "")
		);
		check(diffs, "has_issues", true, details.hasIssues());
		check(diffs, "has_projects", true, details.hasProjects());
		check(diffs, "has_wiki", true, details.hasWiki());
		check(diffs, "allow_merge_commit", false, details.allowMergeCommit());
		check(diffs, "allow_squash_merge", false, details.allowSquashMerge());
		check(diffs, "allow_auto_merge", true, details.allowAutoMerge());
		check(
				diffs,
				"delete_branch_on_merge",
				true,
				details.deleteBranchOnMerge()
		);
	}

	private void checkTopics(
			List<String> diffs,
			RepositoryState actual,
			RepositoryArgs desired
	) {
		List<String> topics = actual.details().topics();
		if (topics == null) {
			topics = List.of();
		}
		checkSets(
				diffs,
				"topics",
				new HashSet<>(desired.topics()),
				new HashSet<>(topics)
		);
	}

	private void checkSecuritySettings(
			List<String> diffs,
			RepositoryState actual
	) {
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
		var sa = actual.details().securityAndAnalysis();
		if (sa != null) {
			boolean secretScanning = sa.secretScanning() != null
					&& SecurityAndAnalysis.StatusObject.Status.ENABLED
							.equals(sa.secretScanning().status());
			boolean secretScanningPush = sa
					.secretScanningPushProtection() != null
					&& SecurityAndAnalysis.StatusObject.Status.ENABLED
							.equals(sa.secretScanningPushProtection().status());
			check(diffs, "secret_scanning", true, secretScanning);
			check(
					diffs,
					"secret_scanning_push_protection",
					true,
					secretScanningPush
			);
		}
	}

	private void checkWorkflowPermissions(
			List<String> diffs,
			RepositoryState actual
	) {
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
	}

	private void checkBranchProtection(
			List<String> diffs,
			RepositoryState actual,
			RepositoryArgs desired
	) {
		boolean isPublic = "public".equals(actual.summary().visibility());
		if (!isPublic) {
			return;
		}

		var bp = actual.branchProtection();
		if (bp == null) {
			diffs.add("branch_protection: missing");
			return;
		}

		check(
				diffs,
				"branch_protection.enforce_admins",
				true,
				bp.enforceAdmins().enabled()
		);
		check(
				diffs,
				"branch_protection.required_linear_history",
				true,
				bp.requiredLinearHistory().enabled()
		);
		check(
				diffs,
				"branch_protection.allow_force_pushes",
				false,
				bp.allowForcePushes().enabled()
		);

		var rsc = bp.requiredStatusChecks();
		boolean strict = rsc != null && rsc.strict();
		check(
				diffs,
				"branch_protection.required_status_checks.strict",
				false,
				strict
		);

		List<String> statusContexts = List.of();
		if (rsc != null) {
			var checks = rsc.checks();
			if (checks != null && !checks.isEmpty()) {
				statusContexts = checks.stream()
						.map(
								BranchProtection.RequiredStatusChecks.StatusCheck::context
						)
						.toList();
			} else {
				var contexts = rsc.contexts();
				statusContexts = contexts != null ? contexts : List.of();
			}
		}

		Set<String> wantContexts = new HashSet<>(BASE_STATUS_CHECKS);
		wantContexts.addAll(desired.requiredStatusChecks());
		checkSets(
				diffs,
				"branch_protection.required_status_checks",
				wantContexts,
				new HashSet<>(statusContexts)
		);
	}

	private void checkSecrets(
			List<String> diffs,
			RepositoryState actual,
			RepositoryArgs desired
	) {
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
	}

	// ─── Fix
	// ──────────────────────────────────────────────────────────────

	List<String> applyFixes(
			String name,
			RepositoryState actual,
			RepositoryArgs desired,
			List<String> diffs
	) throws Exception {
		List<String> remaining = new ArrayList<>(diffs);

		// Special case: archive the repo
		if (desired.archived()) {
			if (remaining.remove("archived")) {
				client.updateRepository(org, name, Map.of("archived", true));
				System.out.printf("[FIXED]   %s: archived%n", name);
			}
			return remaining;
		}

		// Repo settings group (fixable)
		List<String> repoSettingsDiffs = new ArrayList<>();
		checkRepoSettings(repoSettingsDiffs, actual, desired);
		if (!repoSettingsDiffs.isEmpty()) {
			Map<String, Object> fields = new LinkedHashMap<>();
			fields.put("archived", desired.archived());
			fields.put("description", desired.description());
			fields.put("homepage", desired.homepageUrl());
			fields.put("has_issues", true);
			fields.put("has_projects", true);
			fields.put("has_wiki", true);
			fields.put("allow_merge_commit", false);
			fields.put("allow_squash_merge", false);
			fields.put("allow_auto_merge", true);
			fields.put("delete_branch_on_merge", true);
			client.updateRepository(org, name, fields);
			remaining.removeAll(repoSettingsDiffs);
			for (String field : fields.keySet()) {
				System.out.printf("[FIXED]   %s: %s updated%n", name, field);
			}
		}

		// Topics group (fixable)
		List<String> topicsDiffs = new ArrayList<>();
		checkTopics(topicsDiffs, actual, desired);
		if (!topicsDiffs.isEmpty()) {
			client.replaceTopics(org, name, desired.topics());
			remaining.removeAll(topicsDiffs);
			System.out.printf("[FIXED]   %s: topics updated%n", name);
		}

		// Security settings group (fixable)
		List<String> securityDiffs = new ArrayList<>();
		checkSecuritySettings(securityDiffs, actual);
		if (!securityDiffs.isEmpty()) {
			if (securityDiffs.stream()
					.anyMatch(d -> d.startsWith("vulnerability_alerts"))) {
				client.enableVulnerabilityAlerts(org, name);
				System.out.printf(
						"[FIXED]   %s: vulnerability_alerts enabled%n",
						name
				);
			}
			if (securityDiffs.stream()
					.anyMatch(d -> d.startsWith("automated_security_fixes"))) {
				client.enableAutomatedSecurityFixes(org, name);
				System.out.printf(
						"[FIXED]   %s: automated_security_fixes enabled%n",
						name
				);
			}
			if (securityDiffs.stream()
					.anyMatch(d -> d.startsWith("secret_scanning"))) {
				client.updateRepository(
						org,
						name,
						Map.of(
								"security_and_analysis",
								Map.of(
										"secret_scanning",
										Map.of("status", "enabled"),
										"secret_scanning_push_protection",
										Map.of("status", "enabled")
								)
						)
				);
				System.out.printf(
						"[FIXED]   %s: secret_scanning settings updated%n",
						name
				);
			}
			remaining.removeAll(securityDiffs);
		}

		// Workflow permissions (fixable)
		List<String> workflowDiffs = new ArrayList<>();
		checkWorkflowPermissions(workflowDiffs, actual);
		if (!workflowDiffs.isEmpty()) {
			client.updateWorkflowPermissions(
					org,
					name,
					WorkflowPermissions.DefaultWorkflowPermissions.READ,
					true
			);
			remaining.removeAll(workflowDiffs);
			System.out.printf(
					"[FIXED]   %s: workflow_permissions updated%n",
					name
			);
		}

		// Branch protection (NOT fixable yet)
		// Secrets/environments (NOT fixable yet)

		return remaining;
	}

	// ─── Report
	// ──────────────────────────────────────────────────────────────

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

	// ─── Helpers
	// ──────────────────────────────────────────────────────────────

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
