package io.github.arlol.githubcheck;

import java.util.List;

import io.github.arlol.githubcheck.config.RepositoryArgs;

public class GitHubCheck {

	static void main(String[] args) throws Exception {
		String token = System.getenv("GITHUB_TOKEN");
		if (token == null || token.isBlank()) {
			System.err.println(
					"ERROR: GITHUB_TOKEN environment variable not set"
			);
			System.exit(1);
		}

		boolean fix = List.of(args).contains("--fix");

		long startTime = System.currentTimeMillis();

		var checker = new OrgChecker(token, "ArloL", fix);
		CheckResult result = checker.check(repositories());
		checker.printReport(result);

		double totalSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
		System.out
				.printf("%nTotal execution time: %.2f seconds%n", totalSeconds);

		System.exit(result.hasDrift() ? 1 : 0);
	}

	static List<RepositoryArgs> repositories() {
		return List.of(
				RepositoryArgs.create("abenteuer-irland")
						.description("Mum's website for Abenteuer Irland")
						.homepageUrl(
								"https://arlol.github.io/abenteuer-irland/"
						)
						.pages()
						.build(),
				RepositoryArgs.archived("actions"),
				RepositoryArgs.archived("actions-checkout-fetch-depth-demo"),
				RepositoryArgs.create("advent-of-code")
						.description("My advent of code solutions")
						.build(),
				RepositoryArgs.archived("airmac"),
				RepositoryArgs.create("angular-playground")
						.description("A playground for the Angular framework")
						.homepageUrl(
								"https://arlol.github.io/angular-playground/"
						)
						.pages()
						.requiredStatusChecks("pr-check.required-status-check")
						.build(),
				RepositoryArgs.create("arlol.github.io")
						.description("This is the source of my GitHub page")
						.homepageUrl("https://arlol.github.io/")
						.pages()
						.build(),
				RepositoryArgs.create("beatunes-keytocomment")
						.description(
								"A beatunes plugin that writes the key to the comment"
						)
						.build(),
				RepositoryArgs.create("bulma-playground")
						.description("A playground for the Bulma CSS framework")
						.homepageUrl(
								"https://arlol.github.io/bulma-playground/"
						)
						.pages()
						.build(),
				RepositoryArgs.create("business-english")
						.description("Mum's website for Business English")
						.homepageUrl(
								"https://arlol.github.io/business-english/"
						)
						.pages()
						.build(),
				RepositoryArgs.create("calver-tag-action")
						.description(
								"A GitHub Actions action that creates a new version using a CalVer-style derivative and pushes it"
						)
						.build(),
				RepositoryArgs.archived("campuswoche-2018-webseiten-steuern"),
				RepositoryArgs.create("chorito")
						.description(
								"A tool that does some chores in your source code"
						)
						.requiredStatusChecks("main.required-status-check")
						.actionsSecrets("PAT")
						.build(),
				RepositoryArgs.archived("chop-kata"),
				RepositoryArgs.create("corporate-python")
						.description(
								"A container for executing python in corporate environments"
						)
						.actionsSecrets(
								"DOCKER_HUB_ACCESS_TOKEN",
								"DOCKER_HUB_USERNAME"
						)
						.build(),
				RepositoryArgs.create("dependabot-dockerfile-test")
						.description(
								"A test to see whether dependabot updates dockerfiles with args"
						)
						.build(),
				RepositoryArgs.create("dotfiles")
						.description(
								"My collection of dotfiles used to configure my command line environments"
						)
						.build(),
				RepositoryArgs.archived("dotnet-http-client-reproduction"),
				RepositoryArgs.create("eclipse-projects")
						.description(
								"Arlo's project catalog for the Eclipse Installer"
						)
						.homepageUrl(
								"https://arlol.github.io/eclipse-projects/"
						)
						.pages()
						.build(),
				RepositoryArgs.create("effortful-retrieval-questions")
						.description(
								"A collection of effortful retrieval questions of a number of articles I've read"
						)
						.build(),
				RepositoryArgs.create("git-dora-lead-time-calculator")
						.description(
								"A project to calculate the DORA metric lead time with the info from a git repo"
						)
						.requiredStatusChecks("main.required-status-check")
						.build(),
				RepositoryArgs.create("git-presentation-2018-10")
						.description(
								"Git Präsentation für Vorlesung Industrielle Softwareentwicklung"
						)
						.build(),
				RepositoryArgs.archived("gitfx"),
				RepositoryArgs.archived("graalfx"),
				RepositoryArgs.archived("gwt-dragula-test"),
				RepositoryArgs.archived("gwt-log-print-style-demo"),
				RepositoryArgs.archived("gwt-refresh-demo"),
				RepositoryArgs.archived("HalloJSX"),
				RepositoryArgs.archived("HelloCocoaHTTPServer"),
				RepositoryArgs.archived("HelloIntAirActServer"),
				RepositoryArgs.archived("HelloRoutingServer"),
				RepositoryArgs.archived("HelloServer"),
				RepositoryArgs.create("homebrew-tap")
						.description(
								"A homebrew tap for my own formulas and casks"
						)
						.build(),
				RepositoryArgs.archived("iebox"),
				RepositoryArgs.archived("ilabwebworkshop"),
				RepositoryArgs.archived("IntAirAct"),
				RepositoryArgs.archived("IntAirAct-Performance"),
				RepositoryArgs.archived("jBrowserDriver"),
				RepositoryArgs
						.archived("jbrowserdriver-cucumber-integration-tests"),
				RepositoryArgs.archived("jbrowserdriver-test"),
				RepositoryArgs.archived("jdk-newinstance-leak-demo"),
				RepositoryArgs
						.archived("jdk8u144-classloader-leak-demo-webapp"),
				RepositoryArgs.archived("jhipster-app"),
				RepositoryArgs
						.archived("json-smart-dependency-resolution-test"),
				RepositoryArgs.create("kafka-debugger")
						.description(
								"A small jar utility to test kafka connections"
						)
						.build(),
				RepositoryArgs.archived("m2e-wro4j-bug-demo"),
				RepositoryArgs.archived("m2e-wro4j-bug-demo2"),
				RepositoryArgs.archived("maven-quickstart-j2objc"),
				RepositoryArgs.create("menubar-scripts")
						.description(
								"A collection of scripts that can run in e.g. xbar, swiftbar, etc."
						)
						.build(),
				RepositoryArgs.archived("Mirror"),
				RepositoryArgs.archived("modern-ie-vagrant"),
				RepositoryArgs.create("music-stuff")
						.description("Some spotify and beatunes stuff")
						.requiredStatusChecks("test.required-status-check")
						.build(),
				RepositoryArgs.create("mvnx")
						.description(
								"An experiment with Maven dependencies and dynamic classloading"
						)
						.requiredStatusChecks("main.required-status-check")
						.build(),
				RepositoryArgs.archived("MWPhotoBrowser"),
				RepositoryArgs.create("myprojects-cleaner")
						.description(
								"A java application that runs git clean in a bunch of directories"
						)
						.requiredStatusChecks("main.required-status-check")
						.build(),
				RepositoryArgs.create("newlinechecker")
						.description(
								"A sample project to play with GraalVM builds on GitHub Actions"
						)
						.requiredStatusChecks("main.required-status-check")
						.build(),
				RepositoryArgs.create("nope-amine")
						.description(
								"A firefox extension that slowly increases the time for things to load on reddit.com"
						)
						.build(),
				RepositoryArgs.archived("npmrc-github-action"),
				RepositoryArgs.create("open-webui-runner")
						.description(
								"A small repo to run open-webui locally and stop it after using it"
						)
						.build(),
				RepositoryArgs.archived("packer-templates"),
				RepositoryArgs.archived("pico-playground"),
				RepositoryArgs.archived("postgres-query-error-demo"),
				RepositoryArgs.create("postgres-sync-demo")
						.description(
								"A demo on how to use triggers, queues, etc. to sync the app's data somewhere else"
						)
						.build(),
				RepositoryArgs.create("python-nc")
						.description(
								"A test to see if I can implement nc's proxy functionality with python"
						)
						.build(),
				RepositoryArgs.archived("quickstart-buck-bazel-maven"),
				RepositoryArgs.create("rss-to-mail")
						.description(
								"Read from RSS feeds and send an email for every new item"
						)
						.requiredStatusChecks("main.required-status-check")
						.build(),
				RepositoryArgs.create("sci-fi-movies")
						.description(
								"an app to import sci fi movies from rotten tomatoes into a database in order to run queries on them"
						)
						.build(),
				RepositoryArgs.archived("selenium-xp-ie6"),
				RepositoryArgs.archived("self-hosted-gh-actions-runner"),
				RepositoryArgs
						.archived("spring-cloud-context-classloader-leak-demo"),
				RepositoryArgs.archived(
						"spring-configuration-processor-metadata-bug-demo"
				),
				RepositoryArgs
						.archived("spring-security-drupal-password-encoder"),
				RepositoryArgs.create("terraform-github")
						.description(
								"A project to manage github settings with terraform"
						)
						.environment(
								"production",
								env -> env.secrets("TF_GITHUB_TOKEN")
						)
						.build(),
				RepositoryArgs.archived("testcontainers-colima-github-actions"),
				RepositoryArgs.archived("toado"),
				RepositoryArgs.create("tsaf-parser")
						.description("Binary format exploration")
						.requiredStatusChecks("test.required-status-check")
						.build(),
				RepositoryArgs.archived("vagrant-1"),
				RepositoryArgs.create("vagrant-ssh-config")
						.description(
								"A vagrant plugin that automatically creates ssh configs for vms"
						)
						.build(),
				RepositoryArgs.archived("vitest-link-reproduction"),
				RepositoryArgs.archived("vitest-mocking-reproduction"),
				RepositoryArgs.create("wait-for-ports")
						.description(
								"A command-line utility that waits until a port is open"
						)
						.requiredStatusChecks("main.required-status-check")
						.build(),
				RepositoryArgs.create("webapp-classloader-test")
						.description(
								"This is a test that can be used during integration testing to check for classloader leaks"
						)
						.requiredStatusChecks("main.required-status-check")
						.build(),
				RepositoryArgs.create("website-janitor")
						.description(
								"A set of tools that check websites for common misconfigurations or downtime"
						)
						.requiredStatusChecks("main.required-status-check")
						.build(),
				RepositoryArgs.archived("workflow-dispatch-input-defaults")
		);
	}

}
