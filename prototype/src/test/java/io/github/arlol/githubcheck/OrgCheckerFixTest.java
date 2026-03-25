package io.github.arlol.githubcheck;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import io.github.arlol.githubcheck.client.GitHubClient;
import io.github.arlol.githubcheck.config.RepositoryArgs;

@WireMockTest
class OrgCheckerFixTest {

	private OrgChecker checker;

	@BeforeEach
	void setUp(WireMockRuntimeInfo wm) {
		var client = new GitHubClient(wm.getHttpBaseUrl(), "test-token");
		checker = new OrgChecker(client, "ArloL", true);
	}

	@Test
	void noDiffs_noApiCalls() throws Exception {
		List<String> remaining = checker.applyFixes(
				"repo",
				RepositoryArgs.create("repo").build(),
				List.of()
		);
		assertThat(remaining).isEmpty();
		verify(0, patchRequestedFor(urlEqualTo("/repos/ArloL/repo")));
		verify(0, putRequestedFor(urlEqualTo("/repos/ArloL/repo/topics")));
	}

	@Test
	void descriptionDrift_patchesDescription() throws Exception {
		stubFor(
				patch(urlEqualTo("/repos/ArloL/repo")).willReturn(okJson("{}"))
		);

		RepositoryArgs desired = RepositoryArgs.create("repo")
				.description("correct")
				.build();

		List<String> remaining = checker.applyFixes(
				"repo",
				desired,
				List.of("description: want=correct got=wrong")
		);

		assertThat(remaining).isEmpty();
		verify(
				patchRequestedFor(urlEqualTo("/repos/ArloL/repo"))
						.withRequestBody(
								equalToJson("{\"description\":\"correct\"}")
						)
		);
	}

	@Test
	void multipleFieldsDrift_singlePatchCall() throws Exception {
		stubFor(
				patch(urlEqualTo("/repos/ArloL/repo")).willReturn(okJson("{}"))
		);

		RepositoryArgs desired = RepositoryArgs.create("repo")
				.description("correct")
				.homepageUrl("https://example.com")
				.build();

		List<String> remaining = checker.applyFixes(
				"repo",
				desired,
				List.of(
						"description: want=correct got=wrong",
						"homepage_url: want=https://example.com got=",
						"has_wiki: want=true got=false",
						"allow_merge_commit: want=false got=true"
				)
		);

		assertThat(remaining).isEmpty();
		verify(
				1,
				patchRequestedFor(urlEqualTo("/repos/ArloL/repo"))
						.withRequestBody(equalToJson("""
								{
								  "description": "correct",
								  "homepage": "https://example.com",
								  "has_wiki": true,
								  "allow_merge_commit": false
								}
								"""))
		);
	}

	@Test
	void topicsDrift_putsTopics() throws Exception {
		stubFor(
				put(urlEqualTo("/repos/ArloL/repo/topics"))
						.willReturn(okJson("{\"names\":[\"java\"]}"))
		);

		RepositoryArgs desired = RepositoryArgs.create("repo")
				.topics("java")
				.build();

		List<String> remaining = checker
				.applyFixes("repo", desired, List.of("topics missing: [java]"));

		assertThat(remaining).isEmpty();
		verify(
				putRequestedFor(urlEqualTo("/repos/ArloL/repo/topics"))
						.withRequestBody(equalToJson("{\"names\":[\"java\"]}"))
		);
	}

	@Test
	void unfixableDiffs_remainInList() throws Exception {
		stubFor(
				patch(urlEqualTo("/repos/ArloL/repo")).willReturn(okJson("{}"))
		);

		RepositoryArgs desired = RepositoryArgs.create("repo")
				.description("correct")
				.build();

		List<String> remaining = checker.applyFixes(
				"repo",
				desired,
				List.of(
						"description: want=correct got=wrong",
						"default_branch: want=main got=master",
						"vulnerability_alerts: want=true got=false"
				)
		);

		assertThat(remaining).containsExactlyInAnyOrder(
				"default_branch: want=main got=master",
				"vulnerability_alerts: want=true got=false"
		);
	}

	@Test
	void repoFieldsAndTopics_bothFixed() throws Exception {
		stubFor(
				patch(urlEqualTo("/repos/ArloL/repo")).willReturn(okJson("{}"))
		);
		stubFor(
				put(urlEqualTo("/repos/ArloL/repo/topics"))
						.willReturn(okJson("{\"names\":[\"java\"]}"))
		);

		RepositoryArgs desired = RepositoryArgs.create("repo")
				.description("correct")
				.topics("java")
				.build();

		List<String> remaining = checker.applyFixes(
				"repo",
				desired,
				List.of(
						"description: want=correct got=wrong",
						"topics missing: [java]"
				)
		);

		assertThat(remaining).isEmpty();
		verify(
				patchRequestedFor(urlEqualTo("/repos/ArloL/repo"))
						.withRequestBody(
								equalToJson("{\"description\":\"correct\"}")
						)
		);
		verify(
				putRequestedFor(urlEqualTo("/repos/ArloL/repo/topics"))
						.withRequestBody(equalToJson("{\"names\":[\"java\"]}"))
		);
	}

}
