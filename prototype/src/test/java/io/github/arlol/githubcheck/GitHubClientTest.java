package io.github.arlol.githubcheck;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class GitHubClientTest {

	private GitHubClient client;
	private String baseUrl;

	@BeforeEach
	void setUp(WireMockRuntimeInfo wm) {
		baseUrl = wm.getHttpBaseUrl();
		client = new GitHubClient(baseUrl, "test-token");
	}

	// ─── listOrgRepos
	// ───────────────────────────────────────────────────────────

	@Test
	void listOrgRepos_singlePage() throws Exception {
		stubFor(get(urlPathEqualTo("/orgs/ArloL/repos")).willReturn(okJson("""
				[
				  {"name": "repo-a", "archived": false, "visibility": "public"},
				  {"name": "repo-b", "archived": true,  "visibility": "private"}
				]
				""")));

		List<GitHubClient.RepoSummary> repos = client.listOrgRepos("ArloL");

		assertThat(repos).hasSize(2);
		assertThat(repos.get(0).name()).isEqualTo("repo-a");
		assertThat(repos.get(0).archived()).isFalse();
		assertThat(repos.get(0).visibility()).isEqualTo("public");
		assertThat(repos.get(1).name()).isEqualTo("repo-b");
		assertThat(repos.get(1).archived()).isTrue();
		assertThat(repos.get(1).visibility()).isEqualTo("private");
	}

	@Test
	void listOrgRepos_multiPage() throws Exception {
		stubFor(
				get(urlPathEqualTo("/orgs/ArloL/repos"))
						.withQueryParam("page", absent())
						.willReturn(
								okJson(
										"""
												[{"name": "repo-page1", "archived": false, "visibility": "public"}]
												"""
								).withHeader(
										"Link",
										"<" + baseUrl
												+ "/orgs/ArloL/repos?page=2>; rel=\"next\", <"
												+ baseUrl
												+ "/orgs/ArloL/repos?page=2>; rel=\"last\""
								)
						)
		);

		stubFor(
				get(urlPathEqualTo("/orgs/ArloL/repos"))
						.withQueryParam("page", equalTo("2"))
						.willReturn(
								okJson(
										"""
												[{"name": "repo-page2", "archived": false, "visibility": "public"}]
												"""
								)
						)
		);

		List<GitHubClient.RepoSummary> repos = client.listOrgRepos("ArloL");

		assertThat(repos).hasSize(2);
		assertThat(repos.get(0).name()).isEqualTo("repo-page1");
		assertThat(repos.get(1).name()).isEqualTo("repo-page2");
	}

	@Test
	void listOrgRepos_emptyOrg() throws Exception {
		stubFor(
				get(urlPathEqualTo("/orgs/ArloL/repos"))
						.willReturn(okJson("[]"))
		);

		List<GitHubClient.RepoSummary> repos = client.listOrgRepos("ArloL");
		assertThat(repos).isEmpty();
	}

	@Test
	void listOrgRepos_fallsBackToUserEndpointOn404() throws Exception {
		stubFor(
				get(urlPathEqualTo("/orgs/ArloL/repos")).willReturn(
						aResponse().withStatus(404)
								.withHeader("Content-Type", "application/json")
								.withBody(
										"""
												{"message":"Not Found","documentation_url":"https://docs.github.com/rest","status":"404"}
												"""
								)
				)
		);
		stubFor(get(urlPathEqualTo("/user/repos")).willReturn(okJson("""
				[
				  {"name": "repo-a", "archived": false, "visibility": "public"},
				  {"name": "repo-b", "archived": true,  "visibility": "private"}
				]
				""")));

		List<GitHubClient.RepoSummary> repos = client.listOrgRepos("ArloL");

		assertThat(repos).hasSize(2);
		assertThat(repos.get(0).name()).isEqualTo("repo-a");
		assertThat(repos.get(1).name()).isEqualTo("repo-b");
		assertThat(repos.get(1).archived()).isTrue();
	}

	@Test
	void listOrgRepos_errorThrows() {
		stubFor(
				get(urlPathEqualTo("/orgs/ArloL/repos")).willReturn(
						aResponse().withStatus(404)
								.withHeader("Content-Type", "application/json")
								.withBody(
										"""
												{"message":"Not Found","documentation_url":"https://docs.github.com/rest","status":"404"}
												"""
								)
				)
		);
		stubFor(
				get(urlPathEqualTo("/user/repos")).willReturn(
						aResponse().withStatus(403)
								.withHeader("Content-Type", "application/json")
								.withBody("""
										{"message":"Forbidden"}
										""")
				)
		);

		assertThatThrownBy(() -> client.listOrgRepos("ArloL"))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("HTTP 403");
	}

	// ─── getRepo
	// ────────────────────────────────────────────────────────────────

	@Test
	void getRepo_parsesAllFields() throws Exception {
		stubFor(get(urlEqualTo("/repos/ArloL/my-repo")).willReturn(okJson("""
				{
				  "description": "My repo",
				  "homepage": "https://example.com",
				  "has_issues": true,
				  "has_projects": true,
				  "has_wiki": true,
				  "default_branch": "main",
				  "allow_merge_commit": false,
				  "allow_squash_merge": false,
				  "allow_auto_merge": true,
				  "delete_branch_on_merge": true,
				  "security_and_analysis": {
				    "secret_scanning": {"status": "enabled"},
				    "secret_scanning_push_protection": {"status": "enabled"}
				  }
				}
				""")));

		GitHubClient.RepoDetails details = client.getRepo("ArloL", "my-repo");

		assertThat(details.description()).isEqualTo("My repo");
		assertThat(details.homepageUrl()).isEqualTo("https://example.com");
		assertThat(details.hasIssues()).isTrue();
		assertThat(details.hasProjects()).isTrue();
		assertThat(details.hasWiki()).isTrue();
		assertThat(details.defaultBranch()).isEqualTo("main");
		assertThat(details.allowMergeCommit()).isFalse();
		assertThat(details.allowSquashMerge()).isFalse();
		assertThat(details.allowAutoMerge()).isTrue();
		assertThat(details.deleteBranchOnMerge()).isTrue();
		assertThat(details.secretScanning()).isTrue();
		assertThat(details.secretScanningPushProtection()).isTrue();
	}

	@Test
	void getRepo_disabledSecurityAnalysis() throws Exception {
		stubFor(get(urlEqualTo("/repos/ArloL/my-repo")).willReturn(okJson("""
				{
				  "allow_merge_commit": true,
				  "allow_squash_merge": true,
				  "allow_auto_merge": false,
				  "delete_branch_on_merge": false,
				  "security_and_analysis": {
				    "secret_scanning": {"status": "disabled"},
				    "secret_scanning_push_protection": {"status": "disabled"}
				  }
				}
				""")));

		GitHubClient.RepoDetails details = client.getRepo("ArloL", "my-repo");

		assertThat(details.secretScanning()).isFalse();
		assertThat(details.secretScanningPushProtection()).isFalse();
	}

	// ─── getVulnerabilityAlerts
	// ──────────────────────────────────────────────────

	@Test
	void getVulnerabilityAlerts_204_returnsTrue() throws Exception {
		stubFor(
				get(urlEqualTo("/repos/ArloL/my-repo/vulnerability-alerts"))
						.willReturn(aResponse().withStatus(204))
		);

		assertThat(client.getVulnerabilityAlerts("ArloL", "my-repo")).isTrue();
	}

	@Test
	void getVulnerabilityAlerts_404_returnsFalse() throws Exception {
		stubFor(
				get(urlEqualTo("/repos/ArloL/my-repo/vulnerability-alerts"))
						.willReturn(aResponse().withStatus(404))
		);

		assertThat(client.getVulnerabilityAlerts("ArloL", "my-repo")).isFalse();
	}

	@Test
	void getVulnerabilityAlerts_unexpectedStatus_throws() {
		stubFor(
				get(urlEqualTo("/repos/ArloL/my-repo/vulnerability-alerts"))
						.willReturn(aResponse().withStatus(500))
		);

		assertThatThrownBy(
				() -> client.getVulnerabilityAlerts("ArloL", "my-repo")
		).isInstanceOf(RuntimeException.class).hasMessageContaining("500");
	}

	// ─── getAutomatedSecurityFixes
	// ──────────────────────────────────────────────────

	@Test
	void getAutomatedSecurityFixes_enabled_returnsTrue() throws Exception {
		stubFor(
				get(urlEqualTo("/repos/ArloL/my-repo/automated-security-fixes"))
						.willReturn(okJson("""
								{"enabled": true}
								"""))
		);

		assertThat(client.getAutomatedSecurityFixes("ArloL", "my-repo"))
				.isTrue();
	}

	@Test
	void getAutomatedSecurityFixes_disabled_returnsFalse() throws Exception {
		stubFor(
				get(urlEqualTo("/repos/ArloL/my-repo/automated-security-fixes"))
						.willReturn(okJson("""
								{"enabled": false}
								"""))
		);

		assertThat(client.getAutomatedSecurityFixes("ArloL", "my-repo"))
				.isFalse();
	}

	@Test
	void getAutomatedSecurityFixes_404_returnsFalse() throws Exception {
		stubFor(
				get(urlEqualTo("/repos/ArloL/my-repo/automated-security-fixes"))
						.willReturn(aResponse().withStatus(404))
		);

		assertThat(client.getAutomatedSecurityFixes("ArloL", "my-repo"))
				.isFalse();
	}

	@Test
	void getAutomatedSecurityFixes_unexpectedStatus_throws() {
		stubFor(
				get(urlEqualTo("/repos/ArloL/my-repo/automated-security-fixes"))
						.willReturn(aResponse().withStatus(500))
		);

		assertThatThrownBy(
				() -> client.getAutomatedSecurityFixes("ArloL", "my-repo")
		).isInstanceOf(RuntimeException.class).hasMessageContaining("500");
	}

	// ─── getBranchProtection
	// ────────────────────────────────────────────────────

	@Test
	void getBranchProtection_full() throws Exception {
		stubFor(
				get(urlEqualTo("/repos/ArloL/my-repo/branches/main/protection"))
						.willReturn(
								okJson(
										"""
												{
												  "enforce_admins": {"enabled": true},
												  "required_linear_history": {"enabled": true},
												  "required_status_checks": {
												    "checks": [
												      {"context": "check-actions.required-status-check"},
												      {"context": "CodeQL"}
												    ]
												  }
												}
												"""
								)
						)
		);

		Optional<GitHubClient.BranchProtection> opt = client
				.getBranchProtection("ArloL", "my-repo");

		assertThat(opt).isPresent();
		GitHubClient.BranchProtection bp = opt.orElseThrow();
		assertThat(bp.enforceAdmins()).isTrue();
		assertThat(bp.requiredLinearHistory()).isTrue();
		assertThat(bp.requiredStatusCheckContexts()).containsExactlyInAnyOrder(
				"check-actions.required-status-check",
				"CodeQL"
		);
	}

	@Test
	void getBranchProtection_legacyContextsArray() throws Exception {
		stubFor(
				get(urlEqualTo("/repos/ArloL/my-repo/branches/main/protection"))
						.willReturn(
								okJson(
										"""
												{
												  "enforce_admins": {"enabled": false},
												  "required_linear_history": {"enabled": false},
												  "required_status_checks": {
												    "contexts": ["legacy-check-1", "legacy-check-2"]
												  }
												}
												"""
								)
						)
		);

		Optional<GitHubClient.BranchProtection> opt = client
				.getBranchProtection("ArloL", "my-repo");

		assertThat(opt).isPresent();
		assertThat(opt.orElseThrow().requiredStatusCheckContexts())
				.containsExactlyInAnyOrder("legacy-check-1", "legacy-check-2");
	}

	@Test
	void getBranchProtection_404_returnsEmpty() throws Exception {
		stubFor(
				get(urlEqualTo("/repos/ArloL/my-repo/branches/main/protection"))
						.willReturn(aResponse().withStatus(404))
		);

		assertThat(client.getBranchProtection("ArloL", "my-repo")).isEmpty();
	}

	// ─── getActionSecretNames
	// ───────────────────────────────────────────────────

	@Test
	void getActionSecretNames_parsesNames() throws Exception {
		stubFor(
				get(urlPathEqualTo("/repos/ArloL/my-repo/actions/secrets"))
						.willReturn(okJson("""
								{
								  "total_count": 2,
								  "secrets": [
								    {"name": "PAT"},
								    {"name": "DEPLOY_KEY"}
								  ]
								}
								"""))
		);

		List<String> names = client.getActionSecretNames("ArloL", "my-repo");
		assertThat(names).containsExactlyInAnyOrder("PAT", "DEPLOY_KEY");
	}

	@Test
	void getActionSecretNames_empty() throws Exception {
		stubFor(
				get(urlPathEqualTo("/repos/ArloL/my-repo/actions/secrets"))
						.willReturn(okJson("""
								{"total_count": 0, "secrets": []}
								"""))
		);

		assertThat(client.getActionSecretNames("ArloL", "my-repo")).isEmpty();
	}

	// ─── getEnvironmentNames
	// ────────────────────────────────────────────────────

	@Test
	void getEnvironmentNames_parsesNames() throws Exception {
		stubFor(
				get(urlEqualTo("/repos/ArloL/my-repo/environments"))
						.willReturn(okJson("""
								{
								  "total_count": 2,
								  "environments": [
								    {"name": "production"},
								    {"name": "staging"}
								  ]
								}
								"""))
		);

		List<String> names = client.getEnvironmentNames("ArloL", "my-repo");
		assertThat(names).containsExactlyInAnyOrder("production", "staging");
	}

	@Test
	void getEnvironmentNames_empty() throws Exception {
		stubFor(
				get(urlEqualTo("/repos/ArloL/my-repo/environments"))
						.willReturn(okJson("""
								{"total_count": 0, "environments": []}
								"""))
		);

		assertThat(client.getEnvironmentNames("ArloL", "my-repo")).isEmpty();
	}

	// ─── getEnvironmentSecretNames
	// ──────────────────────────────────────────────

	@Test
	void getEnvironmentSecretNames_parsesNames() throws Exception {
		stubFor(
				get(
						urlPathEqualTo(
								"/repos/ArloL/my-repo/environments/production/secrets"
						)
				).willReturn(okJson("""
						{
						  "total_count": 1,
						  "secrets": [{"name": "TF_GITHUB_TOKEN"}]
						}
						"""))
		);

		List<String> names = client
				.getEnvironmentSecretNames("ArloL", "my-repo", "production");
		assertThat(names).containsExactly("TF_GITHUB_TOKEN");
	}

	// ─── getWorkflowPermissions
	// ─────────────────────────────────────────────────

	@Test
	void getWorkflowPermissions_parsesFields() throws Exception {
		stubFor(
				get(
						urlEqualTo(
								"/repos/ArloL/my-repo/actions/permissions/workflow"
						)
				).willReturn(okJson("""
						{
						  "default_workflow_permissions": "read",
						  "can_approve_pull_request_reviews": true
						}
						"""))
		);

		GitHubClient.WorkflowPermissions perms = client
				.getWorkflowPermissions("ArloL", "my-repo");
		assertThat(perms.defaultPermissions()).isEqualTo("read");
		assertThat(perms.canApprovePullRequestReviews()).isTrue();
	}

	@Test
	void getWorkflowPermissions_403_throws() {
		stubFor(
				get(
						urlEqualTo(
								"/repos/ArloL/my-repo/actions/permissions/workflow"
						)
				).willReturn(aResponse().withStatus(403))
		);

		assertThatThrownBy(
				() -> client.getWorkflowPermissions("ArloL", "my-repo")
		).isInstanceOf(RuntimeException.class).hasMessageContaining("403");
	}

	// ─── Authorization header
	// ────────────────────────────────────────────────────

	@Test
	void sendsAuthorizationHeader() throws Exception {
		stubFor(
				get(urlEqualTo("/repos/ArloL/my-repo/environments"))
						.withHeader(
								"Authorization",
								equalTo("Bearer test-token")
						)
						.willReturn(okJson("""
								{"total_count": 0, "environments": []}
								"""))
		);

		// If the header doesn't match, WireMock returns 404 — so successful
		// response confirms header was sent
		client.getEnvironmentNames("ArloL", "my-repo");

		verify(
				getRequestedFor(urlEqualTo("/repos/ArloL/my-repo/environments"))
						.withHeader(
								"Authorization",
								equalTo("Bearer test-token")
						)
		);
	}

}
