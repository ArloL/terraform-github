package io.github.arlol.githubcheck;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.arlol.githubcheck.client.GitHubClient;
import io.github.arlol.githubcheck.client.WorkflowPermissions;
import io.github.arlol.githubcheck.config.RepositoryArgs;

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
				List.of(), // topics
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
				false, // allowForcePushes
				false, // requiredStatusChecksStrict
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(), // actionSecretNames
				Map.of(), // environmentSecretNames
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ, // workflowPermissionsDefault
						true // canApprovePullRequestReviews
				)
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
				List.of(), // topics
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
				false, // allowForcePushes — not checked for archived
				false, // requiredStatusChecksStrict — not checked for archived
				List.of(),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						false // canApprovePullRequestReviews — not checked for
							  // archived
				)
		);
	}

	private static RepositoryArgs defaultArgs() {
		return RepositoryArgs.create("repo").build();
	}

	@Test
	void noDrift_forCorrectPublicRepo() {
		List<String> diffs = checker
				.computeDiffs(goodPublicState("repo"), defaultArgs());
		assertThat(diffs).isEmpty();
	}

	@Test
	void noDrift_forCorrectArchivedRepo() {
		List<String> diffs = checker.computeDiffs(
				goodArchivedState("repo"),
				defaultArgs().toBuilder().archived().build()
		);
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						false
				)
		);
		List<String> diffs = checker.computeDiffs(
				state,
				defaultArgs().toBuilder().archived().build()
		);
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
				List.of(), // topics
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
				false,
				false,
				List.of(),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"branch_protection.required_linear_history: want=true got=false"
		);
	}

	@Test
	void drift_allowForcePushes_isTrue() {
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
				List.of(), // topics
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
				true, // allowForcePushes — should be false
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"branch_protection.allow_force_pushes: want=false got=true"
		);
	}

	@Test
	void drift_requiredStatusChecksStrict_isTrue() {
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
				List.of(), // topics
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
				false,
				true, // requiredStatusChecksStrict — should be false
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"branch_protection.required_status_checks.strict: want=false got=true"
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL"
				), // missing "zizmor"
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, defaultArgs())).anyMatch(
				d -> d.contains("branch_protection.required_status_checks")
						&& d.contains("missing") && d.contains("zizmor")
		);
	}

	@Test
	void drift_missingExtraStatusCheck() {
		var args = defaultArgs().toBuilder()
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				// missing "main.required-status-check"
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor",
						"unexpected-check"
				), // extra check
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, defaultArgs())).anyMatch(
				d -> d.contains("branch_protection.required_status_checks")
						&& d.contains("extra") && d.contains("unexpected-check")
		);
	}

	@Test
	void drift_missingActionSecret() {
		var args = defaultArgs().toBuilder().actionsSecrets("PAT").build();
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(), // missing PAT secret
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of("UNEXPECTED_SECRET"), // unexpected secret
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, defaultArgs())).anyMatch(
				d -> d.contains("action_secrets") && d.contains("extra")
						&& d.contains("UNEXPECTED_SECRET")
		);
	}

	@Test
	void drift_missingEnvironment() {
		var args = defaultArgs().toBuilder()
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(), // missing "production" environment
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, args)).anyMatch(
				d -> d.contains("environments") && d.contains("missing")
						&& d.contains("production")
		);
	}

	@Test
	void drift_missingEnvironmentSecret() {
		var args = defaultArgs().toBuilder()
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of("production", List.of()), // production exists but no
												 // secrets
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, args)).anyMatch(
				d -> d.contains("environment.production.secrets")
						&& d.contains("missing")
						&& d.contains("TF_GITHUB_TOKEN")
		);
	}

	@Test
	void drift_extraEnvironmentSecret() {
		var args = defaultArgs().toBuilder()
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
				List.of(), // topics
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
				false,
				false,
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
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						// should be READ
						WorkflowPermissions.DefaultWorkflowPermissions.WRITE,
						true
				)
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("workflow_permissions.default: want=READ got=WRITE");
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						false
				)
		); // should be true
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"workflow_permissions.can_approve_prs: want=true got=false"
		);
	}

	@Test
	void noDrift_correctEnvironmentWithSecret() {
		var args = defaultArgs().toBuilder()
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of("production", List.of("TF_GITHUB_TOKEN")),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, args)).isEmpty();
	}

	@Test
	void noDrift_correctActionSecret() {
		var args = defaultArgs().toBuilder().actionsSecrets("PAT").build();
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of("PAT"),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, args)).isEmpty();
	}

	@Test
	void noDrift_correctExtraStatusCheck() {
		var args = defaultArgs().toBuilder()
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor",
						"main.required-status-check"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, args)).isEmpty();
	}

	@Test
	void pages_expectsGithubPagesEnvironment() {
		var args = defaultArgs().toBuilder().pages().build();
		// Repo without the github-pages environment
		List<String> diffs = checker
				.computeDiffs(goodPublicState("repo"), args);
		assertThat(diffs).contains("environments missing: [github-pages]");
	}

	@Test
	void pages_noDrift_whenEnvironmentPresent() {
		var args = defaultArgs().toBuilder().pages().build();
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of("github-pages", List.of()),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
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
				List.of(), // topics
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
				false,
				false,
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("automated_security_fixes: want=true got=false");
	}

	@Test
	void noDrift_topicsMatch() {
		RepositoryArgs args = RepositoryArgs.create("repo")
				.topics("java", "maven")
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
				List.of("java", "maven"), // topics
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
				false, // allowForcePushes
				false, // requiredStatusChecksStrict
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, args)).isEmpty();
	}

	@Test
	void drift_topicsMissing() {
		RepositoryArgs args = RepositoryArgs.create("repo")
				.topics("java", "maven")
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
				List.of("java"), // topics — missing "maven"
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
				false, // allowForcePushes
				false, // requiredStatusChecksStrict
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, args))
				.contains("topics missing: [maven]");
	}

	@Test
	void drift_topicsExtra() {
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
				List.of("stale-topic"), // topics — extra
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
				false, // allowForcePushes
				false, // requiredStatusChecksStrict
				List.of(
						"check-actions.required-status-check",
						"codeql-analysis.required-status-check",
						"CodeQL",
						"zizmor"
				),
				List.of(),
				Map.of(),
				new WorkflowPermissions(
						WorkflowPermissions.DefaultWorkflowPermissions.READ,
						true
				)
		);
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("topics extra: [stale-topic]");
	}

}
