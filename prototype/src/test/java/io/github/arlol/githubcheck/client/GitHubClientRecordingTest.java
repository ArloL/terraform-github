package io.github.arlol.githubcheck.client;

import static com.github.tomakehurst.wiremock.client.WireMock.recordSpec;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assumptions.assumeThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

class GitHubClientRecordingTest {

	@RegisterExtension
	static WireMockExtension wm = WireMockExtension.newInstance()
			.options(
					wireMockConfig().dynamicPort()
							.usingFilesUnderDirectory(
									"src/test/resources/wiremock"
							)
			)
			.build();

	@Test
	void record_gitHubApiInteractions() throws Exception {
		String token = System.getenv("GITHUB_TOKEN");
		assumeThat(token).isNotBlank();

		wm.startRecording(
				recordSpec().forTarget("https://api.github.com")
						.makeStubsPersistent(true)
						.ignoreRepeatRequests()
						.build()
		);

		var client = new GitHubClient(
				wm.getRuntimeInfo().getHttpBaseUrl(),
				token
		);
		client.listOrgRepos("ArloL");
		client.getRepo("ArloL", "terraform-github");
		client.getVulnerabilityAlerts("ArloL", "terraform-github");
		client.getWorkflowPermissions("ArloL", "terraform-github");

		wm.stopRecording();
	}

}
