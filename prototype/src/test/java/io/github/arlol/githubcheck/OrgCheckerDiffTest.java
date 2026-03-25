package io.github.arlol.githubcheck;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.arlol.githubcheck.client.BranchProtection;
import io.github.arlol.githubcheck.client.GitHubClient;
import io.github.arlol.githubcheck.client.RepositoryFull;
import io.github.arlol.githubcheck.client.RepositoryMinimal;
import io.github.arlol.githubcheck.client.WorkflowPermissions;
import io.github.arlol.githubcheck.config.RepositoryArgs;

class OrgCheckerDiffTest {

	private static final ObjectMapper MAPPER = new ObjectMapper()
			.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(
					DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
					false
			);

	private static final String GOOD_SUMMARY_JSON = """
			{
				"name": "repo",
				"archived": false,
				"visibility": "public"
			}
			""";

	private static final String GOOD_DETAILS_JSON = """
			{
				"description": "",
				"homepage": "",
				"has_issues": true,
				"has_projects": true,
				"has_wiki": true,
				"default_branch": "main",
				"topics": [],
				"allow_merge_commit": false,
				"allow_squash_merge": false,
				"allow_auto_merge": true,
				"delete_branch_on_merge": true,
				"visibility": "public",
				"archived": false,
				"security_and_analysis": {
					"secret_scanning": {"status": "enabled"},
					"secret_scanning_push_protection": {"status": "enabled"}
				}
			}
			""";

	private static final String GOOD_BRANCH_PROTECTION_JSON = """
			{
				"enforce_admins": {"enabled": true},
				"required_linear_history": {"enabled": true},
				"allow_force_pushes": {"enabled": false},
				"required_status_checks": {
					"strict": false,
					"checks": [
						{"context": "check-actions.required-status-check"},
						{"context": "codeql-analysis.required-status-check"},
						{"context": "CodeQL"},
						{"context": "zizmor"}
					]
				}
			}
			""";

	private static final String GOOD_WORKFLOW_PERMISSIONS_JSON = """
			{
				"default_workflow_permissions": "read",
				"can_approve_pull_request_reviews": true
			}
			""";

	private OrgChecker checker;

	@BeforeEach
	void setUp() {
		checker = new OrgChecker((GitHubClient) null, "ArloL");
	}

	// ─── Helpers
	// ──────────────────────────────────────────────────────────

	private static <T> T parse(String json, Class<T> type) {
		try {
			return MAPPER.readValue(json, type);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static ObjectNode merge(String baseJson, String overridesJson) {
		try {
			ObjectNode base = (ObjectNode) MAPPER.readTree(baseJson);
			ObjectNode overrides = (ObjectNode) MAPPER.readTree(overridesJson);
			base.setAll(overrides);
			return base;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static RepositoryState goodPublicState() {
		return new StateBuilder().build();
	}

	private static RepositoryArgs defaultArgs() {
		return RepositoryArgs.create("repo").build();
	}

	/**
	 * Builder for test RepositoryState with sensible defaults for a "good"
	 * public repo.
	 */
	private static class StateBuilder {

		private String summaryJson = GOOD_SUMMARY_JSON;
		private String detailsJson = GOOD_DETAILS_JSON;
		private boolean vulnerabilityAlerts = true;
		private boolean automatedSecurityFixes = true;
		private String branchProtectionJson = GOOD_BRANCH_PROTECTION_JSON;
		private boolean hasBranchProtection = true;
		private List<String> actionSecretNames = List.of();
		private Map<String, List<String>> environmentSecretNames = Map.of();
		private String workflowPermissionsJson = GOOD_WORKFLOW_PERMISSIONS_JSON;

		StateBuilder summaryOverride(String overridesJson) {
			this.summaryJson = merge(this.summaryJson, overridesJson)
					.toString();
			return this;
		}

		StateBuilder detailsOverride(String overridesJson) {
			this.detailsJson = merge(this.detailsJson, overridesJson)
					.toString();
			return this;
		}

		StateBuilder vulnerabilityAlerts(boolean value) {
			this.vulnerabilityAlerts = value;
			return this;
		}

		StateBuilder automatedSecurityFixes(boolean value) {
			this.automatedSecurityFixes = value;
			return this;
		}

		StateBuilder noBranchProtection() {
			this.hasBranchProtection = false;
			return this;
		}

		StateBuilder branchProtectionOverride(String overridesJson) {
			this.branchProtectionJson = merge(
					this.branchProtectionJson,
					overridesJson
			).toString();
			return this;
		}

		StateBuilder actionSecretNames(String... names) {
			this.actionSecretNames = List.of(names);
			return this;
		}

		StateBuilder environmentSecretNames(
				Map<String, List<String>> envSecrets
		) {
			this.environmentSecretNames = envSecrets;
			return this;
		}

		StateBuilder workflowPermissions(String json) {
			this.workflowPermissionsJson = json;
			return this;
		}

		RepositoryState build() {
			return new RepositoryState(
					"repo",
					parse(summaryJson, RepositoryMinimal.class),
					parse(detailsJson, RepositoryFull.class),
					vulnerabilityAlerts,
					automatedSecurityFixes,
					hasBranchProtection
							? parse(
									branchProtectionJson,
									BranchProtection.class
							)
							: null,
					actionSecretNames,
					environmentSecretNames,
					parse(workflowPermissionsJson, WorkflowPermissions.class)
			);
		}

	}

	// ─── No-drift tests
	// ──────────────────────────────────────────────────────

	@Test
	void noDrift_forCorrectPublicRepo() {
		List<String> diffs = checker
				.computeDiffs(goodPublicState(), defaultArgs());
		assertThat(diffs).isEmpty();
	}

	@Test
	void noDrift_forCorrectArchivedRepo() {
		var state = new StateBuilder().summaryOverride("""
				{"archived": true}
				""")
				.vulnerabilityAlerts(false)
				.automatedSecurityFixes(false)
				.noBranchProtection()
				.workflowPermissions("""
						{
							"default_workflow_permissions": "read",
							"can_approve_pull_request_reviews": false
						}
						""")
				.build();
		List<String> diffs = checker.computeDiffs(
				state,
				defaultArgs().toBuilder().archived().build()
		);
		assertThat(diffs).isEmpty();
	}

	// ─── Repo settings drift
	// ──────────────────────────────────────────────────

	@Test
	void drift_allowMergeCommit_isTrue() {
		var state = new StateBuilder().detailsOverride("""
				{"allow_merge_commit": true}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("allow_merge_commit: want=false got=true");
	}

	@Test
	void drift_allowSquashMerge_isTrue() {
		var state = new StateBuilder().detailsOverride("""
				{"allow_squash_merge": true}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("allow_squash_merge: want=false got=true");
	}

	@Test
	void drift_allowAutoMerge_isFalse() {
		var state = new StateBuilder().detailsOverride("""
				{"allow_auto_merge": false}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("allow_auto_merge: want=true got=false");
	}

	@Test
	void drift_deleteBranchOnMerge_isFalse() {
		var state = new StateBuilder().detailsOverride("""
				{"delete_branch_on_merge": false}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("delete_branch_on_merge: want=true got=false");
	}

	@Test
	void drift_hasIssues_isFalse() {
		var state = new StateBuilder().detailsOverride("""
				{"has_issues": false}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("has_issues: want=true got=false");
	}

	@Test
	void drift_hasProjects_isFalse() {
		var state = new StateBuilder().detailsOverride("""
				{"has_projects": false}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("has_projects: want=true got=false");
	}

	@Test
	void drift_hasWiki_isFalse() {
		var state = new StateBuilder().detailsOverride("""
				{"has_wiki": false}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("has_wiki: want=true got=false");
	}

	@Test
	void drift_defaultBranch_notMain() {
		var state = new StateBuilder().detailsOverride("""
				{"default_branch": "master"}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("default_branch: want=main got=master");
	}

	// ─── Security drift
	// ──────────────────────────────────────────────────────

	@Test
	void drift_vulnerabilityAlerts_isFalse() {
		var state = new StateBuilder().vulnerabilityAlerts(false).build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("vulnerability_alerts: want=true got=false");
	}

	@Test
	void drift_automatedSecurityFixes_isFalse() {
		var state = new StateBuilder().automatedSecurityFixes(false).build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("automated_security_fixes: want=true got=false");
	}

	@Test
	void drift_secretScanning_isFalse() {
		var state = new StateBuilder().detailsOverride("""
				{
					"security_and_analysis": {
						"secret_scanning": {"status": "disabled"},
						"secret_scanning_push_protection": {"status": "enabled"}
					}
				}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("secret_scanning: want=true got=false");
	}

	@Test
	void drift_secretScanningPushProtection_isFalse() {
		var state = new StateBuilder()
				.detailsOverride(
						"""
								{
									"security_and_analysis": {
										"secret_scanning": {"status": "enabled"},
										"secret_scanning_push_protection": {"status": "disabled"}
									}
								}
								"""
				)
				.build();
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"secret_scanning_push_protection: want=true got=false"
		);
	}

	// ─── Archived / private
	// ──────────────────────────────────────────────────

	@Test
	void archived_skipsSecurityAndBranchProtectionChecks() {
		var state = new StateBuilder().summaryOverride("""
				{"archived": true}
				""")
				.vulnerabilityAlerts(false)
				.automatedSecurityFixes(false)
				.detailsOverride(
						"""
								{
									"security_and_analysis": {
										"secret_scanning": {"status": "disabled"},
										"secret_scanning_push_protection": {"status": "disabled"}
									}
								}
								"""
				)
				.noBranchProtection()
				.workflowPermissions("""
						{
							"default_workflow_permissions": "read",
							"can_approve_pull_request_reviews": false
						}
						""")
				.build();
		List<String> diffs = checker.computeDiffs(
				state,
				defaultArgs().toBuilder().archived().build()
		);
		assertThat(diffs).isEmpty();
	}

	@Test
	void privateRepo_skipsBranchProtectionCheck() {
		var state = new StateBuilder().summaryOverride("""
				{"visibility": "private"}
				""").detailsOverride("""
				{"visibility": "private"}
				""").noBranchProtection().build();
		List<String> diffs = checker.computeDiffs(state, defaultArgs());
		assertThat(diffs).isEmpty();
	}

	// ─── Branch protection drift
	// ──────────────────────────────────────────────

	@Test
	void drift_branchProtectionMissing() {
		var state = new StateBuilder().noBranchProtection().build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("branch_protection: missing");
	}

	@Test
	void drift_enforceAdmins_isFalse() {
		var state = new StateBuilder().branchProtectionOverride("""
				{"enforce_admins": {"enabled": false}}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"branch_protection.enforce_admins: want=true got=false"
		);
	}

	@Test
	void drift_requiredLinearHistory_isFalse() {
		var state = new StateBuilder().branchProtectionOverride("""
				{"required_linear_history": {"enabled": false}}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"branch_protection.required_linear_history: want=true got=false"
		);
	}

	@Test
	void drift_allowForcePushes_isTrue() {
		var state = new StateBuilder().branchProtectionOverride("""
				{"allow_force_pushes": {"enabled": true}}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"branch_protection.allow_force_pushes: want=false got=true"
		);
	}

	@Test
	void drift_requiredStatusChecksStrict_isTrue() {
		var state = new StateBuilder()
				.branchProtectionOverride(
						"""
								{
									"required_status_checks": {
										"strict": true,
										"checks": [
											{"context": "check-actions.required-status-check"},
											{"context": "codeql-analysis.required-status-check"},
											{"context": "CodeQL"},
											{"context": "zizmor"}
										]
									}
								}
								"""
				)
				.build();
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"branch_protection.required_status_checks.strict: want=false got=true"
		);
	}

	@Test
	void drift_missingBaseStatusCheck() {
		var state = new StateBuilder()
				.branchProtectionOverride(
						"""
								{
									"required_status_checks": {
										"strict": false,
										"checks": [
											{"context": "check-actions.required-status-check"},
											{"context": "codeql-analysis.required-status-check"},
											{"context": "CodeQL"}
										]
									}
								}
								"""
				)
				.build();
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
		var state = goodPublicState();
		assertThat(checker.computeDiffs(state, args)).anyMatch(
				d -> d.contains("branch_protection.required_status_checks")
						&& d.contains("missing")
						&& d.contains("main.required-status-check")
		);
	}

	@Test
	void drift_extraUnexpectedStatusCheck() {
		var state = new StateBuilder()
				.branchProtectionOverride(
						"""
								{
									"required_status_checks": {
										"strict": false,
										"checks": [
											{"context": "check-actions.required-status-check"},
											{"context": "codeql-analysis.required-status-check"},
											{"context": "CodeQL"},
											{"context": "zizmor"},
											{"context": "unexpected-check"}
										]
									}
								}
								"""
				)
				.build();
		assertThat(checker.computeDiffs(state, defaultArgs())).anyMatch(
				d -> d.contains("branch_protection.required_status_checks")
						&& d.contains("extra") && d.contains("unexpected-check")
		);
	}

	// ─── Secrets / environments drift
	// ──────────────────────────────────────────

	@Test
	void drift_missingActionSecret() {
		var args = defaultArgs().toBuilder().actionsSecrets("PAT").build();
		var state = goodPublicState();
		assertThat(checker.computeDiffs(state, args)).anyMatch(
				d -> d.contains("action_secrets") && d.contains("missing")
						&& d.contains("PAT")
		);
	}

	@Test
	void drift_extraUnexpectedActionSecret() {
		var state = new StateBuilder().actionSecretNames("UNEXPECTED_SECRET")
				.build();
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
		var state = goodPublicState();
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
		var state = new StateBuilder()
				.environmentSecretNames(Map.of("production", List.of()))
				.build();
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
		var state = new StateBuilder().environmentSecretNames(
				Map.of("production", List.of("TF_GITHUB_TOKEN", "EXTRA_SECRET"))
		).build();
		assertThat(checker.computeDiffs(state, args)).anyMatch(
				d -> d.contains("environment.production.secrets")
						&& d.contains("extra") && d.contains("EXTRA_SECRET")
		);
	}

	// ─── Workflow permissions drift
	// ──────────────────────────────────────────

	@Test
	void drift_workflowPermissionsDefault_isWrite() {
		var state = new StateBuilder().workflowPermissions("""
				{
					"default_workflow_permissions": "write",
					"can_approve_pull_request_reviews": true
				}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("workflow_permissions.default: want=READ got=WRITE");
	}

	@Test
	void drift_canApprovePullRequestReviews_isFalse() {
		var state = new StateBuilder().workflowPermissions("""
				{
					"default_workflow_permissions": "read",
					"can_approve_pull_request_reviews": false
				}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs())).contains(
				"workflow_permissions.can_approve_prs: want=true got=false"
		);
	}

	// ─── No-drift (matching) tests
	// ──────────────────────────────────────────

	@Test
	void noDrift_correctEnvironmentWithSecret() {
		var args = defaultArgs().toBuilder()
				.environment(
						"production",
						env -> env.secrets("TF_GITHUB_TOKEN")
				)
				.build();
		var state = new StateBuilder()
				.environmentSecretNames(
						Map.of("production", List.of("TF_GITHUB_TOKEN"))
				)
				.build();
		assertThat(checker.computeDiffs(state, args)).isEmpty();
	}

	@Test
	void noDrift_correctActionSecret() {
		var args = defaultArgs().toBuilder().actionsSecrets("PAT").build();
		var state = new StateBuilder().actionSecretNames("PAT").build();
		assertThat(checker.computeDiffs(state, args)).isEmpty();
	}

	@Test
	void noDrift_correctExtraStatusCheck() {
		var args = defaultArgs().toBuilder()
				.requiredStatusChecks("main.required-status-check")
				.build();
		var state = new StateBuilder()
				.branchProtectionOverride(
						"""
								{
									"required_status_checks": {
										"strict": false,
										"checks": [
											{"context": "check-actions.required-status-check"},
											{"context": "codeql-analysis.required-status-check"},
											{"context": "CodeQL"},
											{"context": "zizmor"},
											{"context": "main.required-status-check"}
										]
									}
								}
								"""
				)
				.build();
		assertThat(checker.computeDiffs(state, args)).isEmpty();
	}

	// ─── Topics drift
	// ──────────────────────────────────────────────────────

	@Test
	void noDrift_topicsMatch() {
		RepositoryArgs args = RepositoryArgs.create("repo")
				.topics("java", "maven")
				.build();
		var state = new StateBuilder().detailsOverride("""
				{"topics": ["java", "maven"]}
				""").build();
		assertThat(checker.computeDiffs(state, args)).isEmpty();
	}

	@Test
	void drift_topicsMissing() {
		RepositoryArgs args = RepositoryArgs.create("repo")
				.topics("java", "maven")
				.build();
		var state = new StateBuilder().detailsOverride("""
				{"topics": ["java"]}
				""").build();
		assertThat(checker.computeDiffs(state, args))
				.contains("topics missing: [maven]");
	}

	@Test
	void drift_topicsExtra() {
		var state = new StateBuilder().detailsOverride("""
				{"topics": ["stale-topic"]}
				""").build();
		assertThat(checker.computeDiffs(state, defaultArgs()))
				.contains("topics extra: [stale-topic]");
	}

	// ─── Pages
	// ──────────────────────────────────────────────────────────

	@Test
	void pages_expectsGithubPagesEnvironment() {
		var args = defaultArgs().toBuilder().pages().build();
		List<String> diffs = checker.computeDiffs(goodPublicState(), args);
		assertThat(diffs).contains("environments missing: [github-pages]");
	}

	@Test
	void pages_noDrift_whenEnvironmentPresent() {
		var args = defaultArgs().toBuilder().pages().build();
		var state = new StateBuilder()
				.environmentSecretNames(Map.of("github-pages", List.of()))
				.build();
		assertThat(checker.computeDiffs(state, args)).isEmpty();
	}

}
