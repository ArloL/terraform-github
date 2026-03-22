package io.github.arlol.githubcheck;

import java.util.List;

public class Main {

	public static void main(String[] args) throws Exception {
		String token = System.getenv("GITHUB_TOKEN");
		if (token == null || token.isBlank()) {
			System.err.println(
					"ERROR: GITHUB_TOKEN environment variable not set"
			);
			System.exit(1);
		}

		long startTime = System.currentTimeMillis();

		var checker = new OrgChecker(token, "ArloL");
		CheckResult result = checker.check(repositories());
		checker.printReport(result);

		double totalSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
		System.out
				.printf("%nTotal execution time: %.2f seconds%n", totalSeconds);

		System.exit(result.hasDrift() ? 1 : 0);
	}

	static List<Repository> repositories() {
		return List.of(
				new Repository(
						"abenteuer-irland",
						RepositoryArgs.builder().githubPages().build()
				),
				new Repository(
						"actions",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"actions-checkout-fetch-depth-demo",
						RepositoryArgs.builder().archived().build()
				),
				new Repository("advent-of-code"),
				new Repository(
						"airmac",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"angular-playground",
						RepositoryArgs.builder()
								.githubPages()
								.requiredStatusChecks(
										"pr-check.required-status-check"
								)
								.build()
				),
				new Repository(
						"arlol.github.io",
						RepositoryArgs.builder().githubPages().build()
				),
				new Repository("beatunes-keytocomment"),
				new Repository(
						"bulma-playground",
						RepositoryArgs.builder().githubPages().build()
				),
				new Repository(
						"business-english",
						RepositoryArgs.builder().githubPages().build()
				),
				new Repository("calver-tag-action"),
				new Repository(
						"campuswoche-2018-webseiten-steuern",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"chorito",
						RepositoryArgs.builder()
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.actionSecrets("PAT")
								.build()
				),
				new Repository(
						"chop-kata",
						RepositoryArgs.builder().archived().build()
				),
				new Repository("corporate-python"),
				new Repository("dependabot-dockerfile-test"),
				new Repository("dotfiles"),
				new Repository(
						"dotnet-http-client-reproduction",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"eclipse-projects",
						RepositoryArgs.builder().githubPages().build()
				),
				new Repository("effortful-retrieval-questions"),
				new Repository(
						"git-dora-lead-time-calculator",
						RepositoryArgs.builder()
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository("git-presentation-2018-10"),
				new Repository(
						"gitfx",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"graalfx",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"gwt-dragula-test",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"gwt-log-print-style-demo",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"gwt-refresh-demo",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"HalloJSX",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"HelloCocoaHTTPServer",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"HelloIntAirActServer",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"HelloRoutingServer",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"HelloServer",
						RepositoryArgs.builder().archived().build()
				),
				new Repository("homebrew-tap"),
				new Repository(
						"iebox",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"ilabwebworkshop",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"IntAirAct",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"IntAirAct-Performance",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"jBrowserDriver",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"jbrowserdriver-cucumber-integration-tests",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"jbrowserdriver-test",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"jdk-newinstance-leak-demo",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"jdk8u144-classloader-leak-demo-webapp",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"jhipster-app",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"json-smart-dependency-resolution-test",
						RepositoryArgs.builder().archived().build()
				),
				new Repository("kafka-debugger"),
				new Repository(
						"m2e-wro4j-bug-demo",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"m2e-wro4j-bug-demo2",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"maven-quickstart-j2objc",
						RepositoryArgs.builder().archived().build()
				),
				new Repository("menubar-scripts"),
				new Repository(
						"Mirror",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"modern-ie-vagrant",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"music-stuff",
						RepositoryArgs.builder()
								.requiredStatusChecks(
										"test.required-status-check"
								)
								.build()
				),
				new Repository(
						"mvnx",
						RepositoryArgs.builder()
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository(
						"MWPhotoBrowser",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"myprojects-cleaner",
						RepositoryArgs.builder()
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository(
						"newlinechecker",
						RepositoryArgs.builder()
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository("nope-amine"),
				new Repository(
						"npmrc-github-action",
						RepositoryArgs.builder().archived().build()
				),
				new Repository("open-webui-runner"),
				new Repository(
						"packer-templates",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"pico-playground",
						RepositoryArgs.builder()
								.archived()
								.githubPages()
								.build()
				),
				new Repository(
						"postgres-query-error-demo",
						RepositoryArgs.builder().archived().build()
				),
				new Repository("postgres-sync-demo"),
				new Repository("python-nc"),
				new Repository(
						"quickstart-buck-bazel-maven",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"rss-to-mail",
						RepositoryArgs.builder()
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository("sci-fi-movies"),
				new Repository(
						"selenium-xp-ie6",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"self-hosted-gh-actions-runner",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"spring-cloud-context-classloader-leak-demo",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"spring-configuration-processor-metadata-bug-demo",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"spring-security-drupal-password-encoder",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"terraform-github",
						RepositoryArgs.builder()
								.environment(
										"production",
										env -> env.secrets("TF_GITHUB_TOKEN")
								)
								.build()
				),
				new Repository(
						"testcontainers-colima-github-actions",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"toado",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"tsaf-parser",
						RepositoryArgs.builder()
								.requiredStatusChecks(
										"test.required-status-check"
								)
								.build()
				),
				new Repository(
						"vagrant-1",
						RepositoryArgs.builder().archived().build()
				),
				new Repository("vagrant-ssh-config"),
				new Repository(
						"vitest-link-reproduction",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"vitest-mocking-reproduction",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"wait-for-ports",
						RepositoryArgs.builder()
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository(
						"webapp-classloader-test",
						RepositoryArgs.builder()
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository(
						"website-janitor",
						RepositoryArgs.builder()
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository(
						"workflow-dispatch-input-defaults",
						RepositoryArgs.builder().archived().build()
				)
		);
	}

}
