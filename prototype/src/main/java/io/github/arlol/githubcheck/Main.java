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
						RepositoryArgs.builder()
								.description(
										"Mum's website for Abenteuer Irland"
								)
								.homepageUrl(
										"https://arlol.github.io/abenteuer-irland/"
								)
								.githubPages()
								.build()
				),
				new Repository(
						"actions",
						RepositoryArgs.builder()
								.description(
										"A demonstration of an automated release workflow for shared workflows and actions"
								)
								.archived()
								.build()
				),
				new Repository(
						"actions-checkout-fetch-depth-demo",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"advent-of-code",
						RepositoryArgs.builder()
								.description("My advent of code solutions")
								.build()
				),
				new Repository(
						"airmac",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"angular-playground",
						RepositoryArgs.builder()
								.description(
										"A playground for the Angular framework"
								)
								.homepageUrl(
										"https://arlol.github.io/angular-playground/"
								)
								.githubPages()
								.requiredStatusChecks(
										"pr-check.required-status-check"
								)
								.build()
				),
				new Repository(
						"arlol.github.io",
						RepositoryArgs.builder()
								.description(
										"This is the source of my GitHub page"
								)
								.homepageUrl("https://arlol.github.io/")
								.githubPages()
								.build()
				),
				new Repository(
						"beatunes-keytocomment",
						RepositoryArgs.builder()
								.description(
										"A beatunes plugin that writes the key to the comment"
								)
								.build()
				),
				new Repository(
						"bulma-playground",
						RepositoryArgs.builder()
								.description(
										"A playground for the Bulma CSS framework"
								)
								.homepageUrl(
										"https://arlol.github.io/bulma-playground/"
								)
								.githubPages()
								.build()
				),
				new Repository(
						"business-english",
						RepositoryArgs.builder()
								.description(
										"Mum's website for Business English"
								)
								.homepageUrl(
										"https://arlol.github.io/business-english/"
								)
								.githubPages()
								.build()
				),
				new Repository(
						"calver-tag-action",
						RepositoryArgs.builder()
								.description(
										"A GitHub Actions action that creates a new version using a CalVer-style derivative and pushes it"
								)
								.build()
				),
				new Repository(
						"campuswoche-2018-webseiten-steuern",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"chorito",
						RepositoryArgs.builder()
								.description(
										"A tool that does some chores in your source code"
								)
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
				new Repository(
						"corporate-python",
						RepositoryArgs.builder()
								.actionSecrets(
										"DOCKER_HUB_ACCESS_TOKEN",
										"DOCKER_HUB_USERNAME"
								)
								.description(
										"A container for executing python in corporate environments"
								)
								.build()
				),
				new Repository(
						"dependabot-dockerfile-test",
						RepositoryArgs.builder()
								.description(
										"A test to see whether dependabot updates dockerfiles with args"
								)
								.build()
				),
				new Repository(
						"dotfiles",
						RepositoryArgs.builder()
								.description(
										"My collection of dotfiles used to configure my command line environments"
								)
								.build()
				),
				new Repository(
						"dotnet-http-client-reproduction",
						RepositoryArgs.builder()
								.description(
										"A reproduction of an issue with the dotnet http client"
								)
								.archived()
								.build()
				),
				new Repository(
						"eclipse-projects",
						RepositoryArgs.builder()
								.description(
										"Arlo's project catalog for the Eclipse Installer"
								)
								.homepageUrl(
										"https://arlol.github.io/eclipse-projects/"
								)
								.githubPages()
								.build()
				),
				new Repository(
						"effortful-retrieval-questions",
						RepositoryArgs.builder()
								.description(
										"A collection of effortful retrieval questions of a number of articles I've read"
								)
								.build()
				),
				new Repository(
						"git-dora-lead-time-calculator",
						RepositoryArgs.builder()
								.description(
										"A project to calculate the DORA metric lead time with the info from a git repo"
								)
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository(
						"git-presentation-2018-10",
						RepositoryArgs.builder()
								.description(
										"Git Präsentation für Vorlesung Industrielle Softwareentwicklung"
								)
								.build()
				),
				new Repository(
						"gitfx",
						RepositoryArgs.builder()
								.description(
										"A simple git client with Java 11+, JavaFX 15+ and GraalVM"
								)
								.archived()
								.build()
				),
				new Repository(
						"graalfx",
						RepositoryArgs.builder()
								.description(
										"A simple desktop app with JavaFX and GraalVM"
								)
								.archived()
								.build()
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
				new Repository(
						"homebrew-tap",
						RepositoryArgs.builder()
								.description(
										"A homebrew tap for my own formulas and casks"
								)
								.build()
				),
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
				new Repository(
						"kafka-debugger",
						RepositoryArgs.builder()
								.description(
										"A small jar utility to test kafka connections"
								)
								.build()
				),
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
				new Repository(
						"menubar-scripts",
						RepositoryArgs.builder()
								.description(
										"A collection of scripts that can run in e.g. xbar, swiftbar, etc."
								)
								.build()
				),
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
								.description("Some spotify and beatunes stuff")
								.requiredStatusChecks(
										"test.required-status-check"
								)
								.build()
				),
				new Repository(
						"mvnx",
						RepositoryArgs.builder()
								.description(
										"An experiment with Maven dependencies and dynamic classloading"
								)
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
								.description(
										"A java application that runs git clean in a bunch of directories"
								)
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository(
						"newlinechecker",
						RepositoryArgs.builder()
								.description(
										"A sample project to play with GraalVM builds on GitHub Actions"
								)
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository(
						"nope-amine",
						RepositoryArgs.builder()
								.description(
										"A firefox extension that slowly increases the time for things to load on reddit.com"
								)
								.build()
				),
				new Repository(
						"npmrc-github-action",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"open-webui-runner",
						RepositoryArgs.builder()
								.description(
										"A small repo to run open-webui locally and stop it after using it"
								)
								.build()
				),
				new Repository(
						"packer-templates",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"pico-playground",
						RepositoryArgs.builder()
								.description(
										"A playground for the Pico CSS framework"
								)
								.homepageUrl(
										"https://arlol.github.io/pico-playground/"
								)
								.archived()
								.githubPages()
								.build()
				),
				new Repository(
						"postgres-query-error-demo",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"postgres-sync-demo",
						RepositoryArgs.builder()
								.description(
										"A demo on how to use triggers, queues, etc. to sync the app's data somewhere else"
								)
								.build()
				),
				new Repository(
						"python-nc",
						RepositoryArgs.builder()
								.description(
										"A test to see if I can implement nc's proxy functionality with python"
								)
								.build()
				),
				new Repository(
						"quickstart-buck-bazel-maven",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"rss-to-mail",
						RepositoryArgs.builder()
								.description(
										"Read from RSS feeds and send an email for every new item"
								)
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository(
						"sci-fi-movies",
						RepositoryArgs.builder()
								.description(
										"an app to import sci fi movies from rotten tomatoes into a database in order to run queries on them"
								)
								.build()
				),
				new Repository(
						"selenium-xp-ie6",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"self-hosted-gh-actions-runner",
						RepositoryArgs.builder()
								.description(
										"An experiment to test docker-based builds inside a GitHub Actions Runner that is a running container itself"
								)
								.archived()
								.build()
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
								.description(
										"A project to manage github settings with terraform"
								)
								.environment(
										"production",
										env -> env.secrets("TF_GITHUB_TOKEN")
								)
								.build()
				),
				new Repository(
						"testcontainers-colima-github-actions",
						RepositoryArgs.builder()
								.description(
										"A demo project of running testcontainers with colima on GitHub Actions"
								)
								.archived()
								.build()
				),
				new Repository(
						"toado",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"tsaf-parser",
						RepositoryArgs.builder()
								.description("Binary format exploration")
								.requiredStatusChecks(
										"test.required-status-check"
								)
								.build()
				),
				new Repository(
						"vagrant-1",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"vagrant-ssh-config",
						RepositoryArgs.builder()
								.description(
										"A vagrant plugin that automatically creates ssh configs for vms"
								)
								.build()
				),
				new Repository(
						"vitest-link-reproduction",
						RepositoryArgs.builder()
								.description(
										"This project demonstrates an issue where vitest runs fail when a dependency is included via a link"
								)
								.archived()
								.build()
				),
				new Repository(
						"vitest-mocking-reproduction",
						RepositoryArgs.builder().archived().build()
				),
				new Repository(
						"wait-for-ports",
						RepositoryArgs.builder()
								.description(
										"A command-line utility that waits until a port is open"
								)
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository(
						"webapp-classloader-test",
						RepositoryArgs.builder()
								.description(
										"This is a test that can be used during integration testing to check for classloader leaks"
								)
								.requiredStatusChecks(
										"main.required-status-check"
								)
								.build()
				),
				new Repository(
						"website-janitor",
						RepositoryArgs.builder()
								.description(
										"A set of tools that check websites for common misconfigurations or downtime"
								)
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
