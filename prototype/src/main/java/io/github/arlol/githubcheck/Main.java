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
				new Repository("abenteuer-irland")
						.description("Mum's website for Abenteuer Irland")
						.homepageUrl(
								"https://arlol.github.io/abenteuer-irland/"
						)
						.githubPages(),
				new Repository("actions").description(
						"A demonstration of an automated release workflow for shared workflows and actions"
				).archived(),
				new Repository("actions-checkout-fetch-depth-demo").archived(),
				new Repository("advent-of-code")
						.description("My advent of code solutions"),
				new Repository("airmac").archived(),
				new Repository("angular-playground")
						.description("A playground for the Angular framework")
						.homepageUrl(
								"https://arlol.github.io/angular-playground/"
						)
						.githubPages()
						.requiredStatusChecks("pr-check.required-status-check"),
				new Repository("arlol.github.io")
						.description("This is the source of my GitHub page")
						.homepageUrl("https://arlol.github.io/")
						.githubPages(),
				new Repository("beatunes-keytocomment").description(
						"A beatunes plugin that writes the key to the comment"
				),
				new Repository("bulma-playground")
						.description("A playground for the Bulma CSS framework")
						.homepageUrl(
								"https://arlol.github.io/bulma-playground/"
						)
						.githubPages(),
				new Repository("business-english")
						.description("Mum's website for Business English")
						.homepageUrl(
								"https://arlol.github.io/business-english/"
						)
						.githubPages(),
				new Repository("calver-tag-action").description(
						"A GitHub Actions action that creates a new version using a CalVer-style derivative and pushes it"
				),
				new Repository("campuswoche-2018-webseiten-steuern").archived(),
				new Repository("chorito").description(
						"A tool that does some chores in your source code"
				)
						.requiredStatusChecks("main.required-status-check")
						.actionSecrets("PAT"),
				new Repository("chop-kata").archived(),
				new Repository("corporate-python").description(
						"A container for executing python in corporate environments"
)
						.actionSecrets(
								"DOCKER_HUB_ACCESS_TOKEN",
								"DOCKER_HUB_USERNAME"
				),
				new Repository("dependabot-dockerfile-test").description(
						"A test to see whether dependabot updates dockerfiles with args"
				),
				new Repository("dotfiles").description(
						"My collection of dotfiles used to configure my command line environments"
				),
				new Repository("dotnet-http-client-reproduction").description(
						"A reproduction of an issue with the dotnet http client"
				).archived(),
				new Repository("eclipse-projects").description(
						"Arlo's project catalog for the Eclipse Installer"
				)
						.homepageUrl(
								"https://arlol.github.io/eclipse-projects/"
						)
						.githubPages(),
				new Repository("effortful-retrieval-questions").description(
						"A collection of effortful retrieval questions of a number of articles I've read"
				),
				new Repository("git-dora-lead-time-calculator").description(
						"A project to calculate the DORA metric lead time with the info from a git repo"
				).requiredStatusChecks("main.required-status-check"),
				new Repository("git-presentation-2018-10").description(
						"Git Präsentation für Vorlesung Industrielle Softwareentwicklung"
				),
				new Repository("gitfx").description(
						"A simple git client with Java 11+, JavaFX 15+ and GraalVM"
				).archived(),
				new Repository("graalfx")
						.description(
								"A simple desktop app with JavaFX and GraalVM"
						)
						.archived(),
				new Repository("gwt-dragula-test").archived(),
				new Repository("gwt-log-print-style-demo").archived(),
				new Repository("gwt-refresh-demo").archived(),
				new Repository("HalloJSX").archived(),
				new Repository("HelloCocoaHTTPServer").archived(),
				new Repository("HelloIntAirActServer").archived(),
				new Repository("HelloRoutingServer").archived(),
				new Repository("HelloServer").archived(),
				new Repository("homebrew-tap").description(
						"A homebrew tap for my own formulas and casks"
				),
				new Repository("iebox").archived(),
				new Repository("ilabwebworkshop").archived(),
				new Repository("IntAirAct").archived(),
				new Repository("IntAirAct-Performance").archived(),
				new Repository("jBrowserDriver").archived(),
				new Repository("jbrowserdriver-cucumber-integration-tests")
						.archived(),
				new Repository("jbrowserdriver-test").archived(),
				new Repository("jdk-newinstance-leak-demo").archived(),
				new Repository("jdk8u144-classloader-leak-demo-webapp")
						.archived(),
				new Repository("jhipster-app").archived(),
				new Repository("json-smart-dependency-resolution-test")
						.archived(),
				new Repository("kafka-debugger").description(
						"A small jar utility to test kafka connections"
				),
				new Repository("m2e-wro4j-bug-demo").archived(),
				new Repository("m2e-wro4j-bug-demo2").archived(),
				new Repository("maven-quickstart-j2objc").archived(),
				new Repository("menubar-scripts").description(
						"A collection of scripts that can run in e.g. xbar, swiftbar, etc."
				),
				new Repository("Mirror").archived(),
				new Repository("modern-ie-vagrant").archived(),
				new Repository("music-stuff")
						.description("Some spotify and beatunes stuff")
						.requiredStatusChecks("test.required-status-check"),
				new Repository("mvnx").description(
						"An experiment with Maven dependencies and dynamic classloading"
				).requiredStatusChecks("main.required-status-check"),
				new Repository("MWPhotoBrowser").archived(),
				new Repository("myprojects-cleaner").description(
						"A java application that runs git clean in a bunch of directories"
				).requiredStatusChecks("main.required-status-check"),
				new Repository("newlinechecker").description(
						"A sample project to play with GraalVM builds on GitHub Actions"
				).requiredStatusChecks("main.required-status-check"),
				new Repository("nope-amine").description(
						"A firefox extension that slowly increases the time for things to load on reddit.com"
				),
				new Repository("npmrc-github-action").archived(),
				new Repository("open-webui-runner").description(
						"A small repo to run open-webui locally and stop it after using it"
				),
				new Repository("packer-templates").archived(),
				new Repository("pico-playground")
						.description("A playground for the Pico CSS framework")
						.homepageUrl("https://arlol.github.io/pico-playground/")
						.githubPages()
						.archived(),
				new Repository("postgres-query-error-demo").archived(),
				new Repository("postgres-sync-demo").description(
						"A demo on how to use triggers, queues, etc. to sync the app's data somewhere else"
				),
				new Repository("python-nc").description(
						"A test to see if I can implement nc's proxy functionality with python"
				),
				new Repository("quickstart-buck-bazel-maven").archived(),
				new Repository("rss-to-mail").description(
						"Read from RSS feeds and send an email for every new item"
				).requiredStatusChecks("main.required-status-check"),
				new Repository("sci-fi-movies").description(
						"an app to import sci fi movies from rotten tomatoes into a database in order to run queries on them"
				),
				new Repository("selenium-xp-ie6").archived(),
				new Repository("self-hosted-gh-actions-runner").description(
						"An experiment to test docker-based builds inside a GitHub Actions Runner that is a running container itself"
				).archived(),
				new Repository("spring-cloud-context-classloader-leak-demo")
						.archived(),
				new Repository(
						"spring-configuration-processor-metadata-bug-demo"
				).archived(),
				new Repository("spring-security-drupal-password-encoder")
						.archived(),
				new Repository("terraform-github").description(
						"A project to manage github settings with terraform"
				)
						.environment(
								"production",
								env -> env.secrets("TF_GITHUB_TOKEN")
						),
				new Repository(
						"testcontainers-colima-github-actions"
				).description(
						"A demo project of running testcontainers with colima on GitHub Actions"
				).archived(),
				new Repository("toado").archived(),
				new Repository("tsaf-parser")
						.description("Binary format exploration")
						.requiredStatusChecks("test.required-status-check"),
				new Repository("vagrant-1").archived(),
				new Repository("vagrant-ssh-config").description(
						"A vagrant plugin that automatically creates ssh configs for vms"
				),
				new Repository("vitest-link-reproduction").description(
						"This project demonstrates an issue where vitest runs fail when a dependency is included via a link"
				).archived(),
				new Repository("vitest-mocking-reproduction").archived(),
				new Repository("wait-for-ports").description(
						"A command-line utility that waits until a port is open"
				).requiredStatusChecks("main.required-status-check"),
				new Repository("webapp-classloader-test").description(
						"This is a test that can be used during integration testing to check for classloader leaks"
				).requiredStatusChecks("main.required-status-check"),
				new Repository("website-janitor").description(
						"A set of tools that check websites for common misconfigurations or downtime"
				).requiredStatusChecks("main.required-status-check"),
				new Repository("workflow-dispatch-input-defaults").archived()
		);
	}

}
