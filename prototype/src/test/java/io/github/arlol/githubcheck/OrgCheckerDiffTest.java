package io.github.arlol.githubcheck;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrgCheckerDiffTest {

	private OrgChecker checker;

	@BeforeEach
	void setUp() {
		checker = new OrgChecker((GitHubClient) null, "ArloL");
	}

	// Helper: a fully-correct public non-archived repo state
	private static RepositoryState goodPublicState(String name) {
		return new RepositoryState(
				name,
				false, // archived
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false, // allowMergeCommit
				false, // allowSquashMerge
				true, // allowAutoMerge
				true, // deleteBranchOnMerge
				true, // vulnerabilityAlerts
				true, // automatedSecurityFixes
				true, // secretScanning
				true, // secretScanningPushProtection
				true, // branchProtectionExists
				true, // enforceAdmins
				true, // requiredLinearHistory
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(), // actionSecretNames
				Map.of(), // environmentSecretNames
				"read", // workflowPermissionsDefault
				true // canApprovePullRequestReviews
		);
	}

	// Helper: a fully-correct archived repo state
	private static RepositoryState goodArchivedState(String name) {
		return new RepositoryState(
				name,
				true, // archived
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false, // allowMergeCommit
				false, // allowSquashMerge
				true, // allowAutoMerge
				true, // deleteBranchOnMerge
				false, // vulnerabilityAlerts — not checked for archived
				true, // automatedSecurityFixes
				false, // secretScanning — not checked for archived
				false, // secretScanningPushProtection — not checked for
					   // archived
				false, // branchProtectionExists — not checked for archived
				false, // enforceAdmins — not checked for archived
				false, // requiredLinearHistory — not checked for archived
				List.of(),
				List.of(),
				Map.of(),
				"read",
				false // canApprovePullRequestReviews — not checked for archived
		);
	}

	private static RepositoryArgs defaultArgs() {
		return RepositoryArgs.builder().build();
	}

	@Test
	void noDrift_forCorrectPublicRepo() {
		List<String> diffs = checker
				.computeDiffs(goodPublicState("repo"), defaultArgs());
		assertThat(diffs).isEmpty();
	}

	@Test
	void noDrift_forCorrectArchivedRepo() {
		RepositoryArgs args = RepositoryArgs.builder().archived().build();
		List<String> diffs = checker
				.computeDiffs(goodArchivedState("repo"), args);
		assertThat(diffs).isEmpty();
	}

	@Test
	void drift_allowMergeCommit_isTrue() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				true, // allowMergeCommit — should be false
				false,
				false,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("allow_merge_commit: want=false got=true");
	}

	@Test
	void drift_allowSquashMerge_isTrue() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				true, // allowSquashMerge — should be false
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("allow_squash_merge: want=false got=true");
	}

	@Test
	void drift_allowAutoMerge_isFalse() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				false, // allowAutoMerge — should be true
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("allow_auto_merge: want=true got=false");
	}

	@Test
	void drift_deleteBranchOnMerge_isFalse() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				false, // deleteBranchOnMerge — should be true
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("delete_branch_on_merge: want=true got=false");
	}

	@Test
	void drift_vulnerabilityAlerts_isFalse() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				false, // vulnerabilityAlerts — should be true
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("vulnerability_alerts: want=true got=false");
	}

	@Test
	void drift_secretScanning_isFalse() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				false, // secretScanning — should be true
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("secret_scanning: want=true got=false");
	}

	@Test
	void drift_secretScanningPushProtection_isFalse() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				false, // secretScanningPushProtection — should be true
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"secret_scanning_push_protection: want=true got=false"
		);
	}

	@Test
	void archived_skipsSecurityAndBranchProtectionChecks() {
		// An archived repo that has wrong security settings — should not flag
		// them
		RepositoryState state = new RepositoryState(
				"repo",
				true,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				false,
				true, // automatedSecurityFixes
				false,
				false, // security all false — should be ignored for archived
				false,
				false,
				false, // branch protection all false — should be ignored for
					   // archived
				List.of(),
				List.of(),
				Map.of(),
				"read",
				false
		);
		RepositoryArgs args = RepositoryArgs.builder().archived().build();
		List<String> diffs = checker.computeDiffs(state, args);
		assertThat(diffs).isEmpty();
	}

	@Test
	void privateRepo_skipsBranchProtectionCheck() {
		// A private repo with correct other settings but no branch protection —
		// ok
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"private",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				false,
				false,
				false, // no branch protection — ok for private
				List.of(),
				List.of(),
				Map.of(),
				"read",
				true
		);
		List<String> diffs = checker.computeDiffs(state, defaultArgs());
		assertThat(diffs).isEmpty();
	}

	@Test
	void drift_branchProtectionMissing() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				false, // branchProtectionExists — should exist for public repos
				false,
				false,
				List.of(),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("branch_protection: missing");
	}

	@Test
	void drift_enforceAdmins_isFalse() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				false, // enforceAdmins — should be true
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"branch_protection.enforce_admins: want=true got=false"
		);
	}

	@Test
	void drift_requiredLinearHistory_isFalse() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				false, // requiredLinearHistory — should be true
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"branch_protection.required_linear_history: want=true got=false"
		);
	}

	@Test
	void drift_missingBaseStatusCheck() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL"
				), // missing "zizmor"
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs())).anyMatch(
				d -> d.contains("branch_protection.required_status_checks")
						&& d.contains("missing") && d.contains("zizmor")
		);
	}

	@Test
	void drift_missingExtraStatusCheck() {
		RepositoryArgs args = RepositoryArgs.builder()
				.requiredStatusChecks("main.required-status-check")
				.build();
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				// missing "main.required-status-check"
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, args)).anyMatch(
				d -> d.contains("branch_protection.required_status_checks")
						&& d.contains("missing")
						&& d.contains("main.required-status-check")
		);
	}

	@Test
	void drift_extraUnexpectedStatusCheck() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor",
						"unexpected-check"
				), // extra check
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs())).anyMatch(
				d -> d.contains("branch_protection.required_status_checks")
						&& d.contains("extra") && d.contains("unexpected-check")
		);
	}

	@Test
	void drift_missingActionSecret() {
		RepositoryArgs args = RepositoryArgs.builder()
				.actionSecrets("PAT")
				.build();
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(), // missing PAT secret
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, args)).anyMatch(
				d -> d.contains("action_secrets") && d.contains("missing")
						&& d.contains("PAT")
		);
	}

	@Test
	void drift_extraUnexpectedActionSecret() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of("UNEXPECTED_SECRET"), // unexpected secret
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs())).anyMatch(
				d -> d.contains("action_secrets") && d.contains("extra")
						&& d.contains("UNEXPECTED_SECRET")
		);
	}

	@Test
	void drift_missingEnvironment() {
		RepositoryArgs args = RepositoryArgs.builder()
				.environment("production", env -> env.secrets("TF_TOKEN"))
				.build();
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(), // missing "production" environment
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, args)).anyMatch(
				d -> d.contains("environments") && d.contains("missing")
						&& d.contains("production")
		);
	}

	@Test
	void drift_missingEnvironmentSecret() {
		RepositoryArgs args = RepositoryArgs.builder()
				.environment(
						"production",
						env -> env.secrets("TF_GITHUB_TOKEN")
				)
				.build();
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of("production", List.of()), // production exists but no
												 // secrets
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, args)).anyMatch(
				d -> d.contains("environment.production.secrets")
						&& d.contains("missing")
						&& d.contains("TF_GITHUB_TOKEN")
		);
	}

	@Test
	void drift_extraEnvironmentSecret() {
		RepositoryArgs args = RepositoryArgs.builder()
				.environment(
						"production",
						env -> env.secrets("TF_GITHUB_TOKEN")
				)
				.build();
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(
						"production",
						List.of("TF_GITHUB_TOKEN", "EXTRA_SECRET")
				),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, args)).anyMatch(
				d -> d.contains("environment.production.secrets")
						&& d.contains("extra") && d.contains("EXTRA_SECRET")
		);
	}

	@Test
	void drift_workflowPermissionsDefault_isWrite() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"write", // should be "read"
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("workflow_permissions.default: want=read got=write");
	}

	@Test
	void drift_canApprovePullRequestReviews_isFalse() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				false
		); // should be true
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"workflow_permissions.can_approve_prs: want=true got=false"
		);
	}

	@Test
	void noDrift_correctEnvironmentWithSecret() {
		RepositoryArgs args = RepositoryArgs.builder()
				.environment(
						"production",
						env -> env.secrets("TF_GITHUB_TOKEN")
				)
				.build();
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of("production", List.of("TF_GITHUB_TOKEN")),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, args)).isEmpty();
	}

	@Test
	void noDrift_correctActionSecret() {
		RepositoryArgs args = RepositoryArgs.builder()
				.actionSecrets("PAT")
				.build();
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of("PAT"),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, args)).isEmpty();
	}

	@Test
	void noDrift_correctExtraStatusCheck() {
		RepositoryArgs args = RepositoryArgs.builder()
				.requiredStatusChecks("main.required-status-check")
				.build();
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor",
						"main.required-status-check"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, args)).isEmpty();
	}

	@Test
	void pages_expectsGithubPagesEnvironment() {
		RepositoryArgs args = RepositoryArgs.builder().pages().build();
		// Repo without the github-pages environment
		List<String> diffs = checker
				.computeDiffs(goodPublicState("repo"), args);
		assertThat(diffs).contains("environments missing: [github-pages]");
	}

	@Test
	void pages_noDrift_whenEnvironmentPresent() {
		RepositoryArgs args = RepositoryArgs.builder().pages().build();
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true, // hasIssues
				true, // hasProjects
				true, // hasWiki
				"main", // defaultBranch
				false,
				false,
				true,
				true,
				true,
				true, // automatedSecurityFixes
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of("github-pages", List.of()),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, args)).isEmpty();
	}

	@Test
	void drift_hasIssues_isFalse() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				false, // hasIssues — should be true
				true,
				true,
				"main",
				false,
				false,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("has_issues: want=true got=false");
	}

	@Test
	void drift_hasProjects_isFalse() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true,
				false, // hasProjects — should be true
				true,
				"main",
				false,
				false,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("has_projects: want=true got=false");
	}

	@Test
	void drift_hasWiki_isFalse() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true,
				true,
				false, // hasWiki — should be true
				"main",
				false,
				false,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("has_wiki: want=true got=false");
	}

	@Test
	void drift_defaultBranch_notMain() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true,
				true,
				true,
				"master", // defaultBranch — should be main
				false,
				false,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("default_branch: want=main got=master");
	}

	@Test
	void drift_automatedSecurityFixes_isFalse() {
		RepositoryState state = new RepositoryState(
				"repo",
				false,
				"public",
				"", // description
				"", // homepageUrl
				true,
				true,
				true,
				"main",
				false,
				false,
				true,
				true,
				true,
				false, // automatedSecurityFixes — should be true
				true,
				true,
				true,
				true,
				true,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				"read",
				true
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("automated_security_fixes: want=true got=false");
	}

}
