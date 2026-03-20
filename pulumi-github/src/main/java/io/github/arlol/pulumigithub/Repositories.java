package io.github.arlol.pulumigithub;

import java.util.List;

public class Repositories {

    public static final List<RepositoryConfig> ALL = List.of(
            RepositoryConfig.builder("abenteuer-irland", "Mum's website for Abenteuer Irland")
                    .homepage("https://arlol.github.io/abenteuer-irland/")
                    .githubPages(true)
                    .build(),
            RepositoryConfig.builder("actions",
                    "A demonstration of an automated release workflow for shared workflows and actions")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("actions-checkout-fetch-depth-demo", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("advent-of-code", "My advent of code solutions")
                    .build(),
            RepositoryConfig.builder("airmac", "A GitHub repository of Airmac")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("angular-playground", "A playground for the Angular framework")
                    .homepage("https://arlol.github.io/angular-playground/")
                    .githubPages(true)
                    .requiredStatusChecks("pr-check.required-status-check")
                    .build(),
            RepositoryConfig.builder("arlol.github.io", "This is the source of my GitHub page")
                    .homepage("https://arlol.github.io/")
                    .githubPages(true)
                    .build(),
            RepositoryConfig.builder("beatunes-keytocomment",
                    "A beatunes plugin that writes the key to the comment")
                    .build(),
            RepositoryConfig.builder("bulma-playground", "A playground for the Bulma CSS framework")
                    .homepage("https://arlol.github.io/bulma-playground/")
                    .githubPages(true)
                    .build(),
            RepositoryConfig.builder("business-english", "Mum's website for Business English")
                    .homepage("https://arlol.github.io/business-english/")
                    .githubPages(true)
                    .build(),
            RepositoryConfig.builder("calver-tag-action",
                    "A GitHub Actions action that creates a new version using a CalVer-style derivative and pushes it")
                    .build(),
            RepositoryConfig.builder("campuswoche-2018-webseiten-steuern", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("chop-kata", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("chorito", "A tool that does some chores in your source code")
                    .actionsSecrets("PAT")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("corporate-python",
                    "A container for executing python in corporate environments")
                    .build(),
            RepositoryConfig.builder("dependabot-dockerfile-test",
                    "A test to see whether dependabot updates dockerfiles with args")
                    .build(),
            RepositoryConfig.builder("dotfiles",
                    "My collection of dotfiles used to configure my command line environments")
                    .build(),
            RepositoryConfig.builder("dotnet-http-client-reproduction",
                    "A reproduction of an issue with the dotnet http client")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("eclipse-projects", "Arlo's project catalog for the Eclipse Installer")
                    .homepage("https://arlol.github.io/eclipse-projects/")
                    .githubPages(true)
                    .build(),
            RepositoryConfig.builder("effortful-retrieval-questions",
                    "A collection of effortful retrieval questions of a number of articles I've read")
                    .build(),
            RepositoryConfig.builder("git-dora-lead-time-calculator",
                    "A project to calculate the DORA metric lead time with the info from a git repo")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("git-presentation-2018-10",
                    "Git Präsentation für Vorlesung Industrielle Softwareentwicklung")
                    .build(),
            RepositoryConfig.builder("gitfx", "A simple git client with Java 11+, JavaFX 15+ and GraalVM")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("graalfx", "A simple desktop app with JavaFX and GraalVM")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("gwt-dragula-test", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("gwt-log-print-style-demo",
                    "A demonstration of a bug with gwt-log and using media print queries in Internet Explorer 9.")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("gwt-refresh-demo", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("HalloJSX",
                    "A quick implementation of an idea to use HalloJS in an OSX editor.")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("HelloCocoaHTTPServer", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("HelloIntAirActServer", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("HelloRoutingServer", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("HelloServer", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("homebrew-tap", "A homebrew tap for my own formulas and casks")
                    .build(),
            RepositoryConfig.builder("iebox",
                    "An improved installer of the IE Application Compatibility VMs for Linux and OS X using VirtualBox")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("ilabwebworkshop", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("IntAirAct", "A Framework enabling Device Interaction using REST.")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("IntAirAct-Performance", "Tests to show IntAirAct's performance.")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("jBrowserDriver",
                    "A programmable, embeddable web browser driver compatible with the Selenium WebDriver spec -- headless, WebKit-based, pure Java")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("jbrowserdriver-cucumber-integration-tests",
                    "A quick demo of how to use jBrowserDriver for Cucumber BDD Tests in a Spring Boot application.")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("jbrowserdriver-test", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("jdk-newinstance-leak-demo", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("jdk8u144-classloader-leak-demo-webapp", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("jhipster-app", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("json-smart-dependency-resolution-test", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("kafka-debugger", "A small jar utility to test kafka connections")
                    .build(),
            RepositoryConfig.builder("m2e-wro4j-bug-demo", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("m2e-wro4j-bug-demo2", "Maven web app to demonstrate an issue")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("maven-quickstart-j2objc", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("menubar-scripts",
                    "A collection of scripts that can run in e.g. xbar, swiftbar, etc.")
                    .build(),
            RepositoryConfig.builder("Mirror", "A clone of Airmac using RoutingHTTPServer.")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("modern-ie-vagrant", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("music-stuff", "Some spotify and beatunes stuff")
                    .requiredStatusChecks("test.required-status-check")
                    .build(),
            RepositoryConfig.builder("mvnx",
                    "An experiment with Maven dependencies and dynamic classloading")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("MWPhotoBrowser", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("myprojects-cleaner",
                    "A java application that runs git clean in a bunch of directories")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("newlinechecker",
                    "A sample project to play with GraalVM builds on GitHub Actions")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("nope-amine",
                    "A firefox extension that slowly increases the time for things to load on reddit.com")
                    .build(),
            RepositoryConfig.builder("npmrc-github-action", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("open-webui-runner",
                    "A small repo to run open-webui locally and stop it after using it")
                    .build(),
            RepositoryConfig.builder("packer-templates", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("pico-playground", "A playground for the Pico CSS framework")
                    .homepage("https://arlol.github.io/pico-playground/")
                    .githubPages(true)
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("postgres-query-error-demo", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("postgres-sync-demo",
                    "A demo on how to use triggers, queues, etc. to sync the app's data somewhere else")
                    .build(),
            RepositoryConfig.builder("python-nc",
                    "A test to see if I can implement nc's proxy functionality with python")
                    .build(),
            RepositoryConfig.builder("quickstart-buck-bazel-maven", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("rss-to-mail",
                    "Read from RSS feeds and send an email for every new item")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("sci-fi-movies",
                    "an app to import sci fi movies from rotten tomatoes into a database in order to run queries on them")
                    .build(),
            RepositoryConfig.builder("selenium-xp-ie6", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("self-hosted-gh-actions-runner",
                    "An experiment to test docker-based builds inside a GitHub Actions Runner that is a running container itself")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("spring-cloud-context-classloader-leak-demo", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("spring-configuration-processor-metadata-bug-demo", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("spring-security-drupal-password-encoder", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("terraform-github",
                    "A project to manage github settings with terraform")
                    .environments(List.of(
                            new EnvironmentConfig("production", List.of("TF_GITHUB_TOKEN"))))
                    .build(),
            RepositoryConfig.builder("testcontainers-colima-github-actions",
                    "A demo project of running testcontainers with colima on GitHub Actions")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("toado", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("tsaf-parser", "Binary format exploration")
                    .requiredStatusChecks("test.required-status-check")
                    .build(),
            RepositoryConfig.builder("vagrant-1", "openSUSE for Vagrant")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("vagrant-ssh-config",
                    "A vagrant plugin that automatically creates ssh configs for vms")
                    .build(),
            RepositoryConfig.builder("vitest-link-reproduction",
                    "This project demonstrates an issue where vitest runs fail when a dependency is included via a link")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("vitest-mocking-reproduction", "")
                    .archived(true)
                    .build(),
            RepositoryConfig.builder("wait-for-ports",
                    "A command-line utility that waits until a port is open")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("webapp-classloader-test",
                    "This is a test that can be used during integration testing to check for classloader leaks")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("website-janitor",
                    "A set of tools that check websites for common misconfigurations or downtime")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("workflow-dispatch-input-defaults", "")
                    .archived(true)
                    .build()
    );

}
