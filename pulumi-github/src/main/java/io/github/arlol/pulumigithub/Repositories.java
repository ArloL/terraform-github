package io.github.arlol.pulumigithub;

import java.util.List;

public class Repositories {

    public static final List<RepositoryConfig> ALL = List.of(
            RepositoryConfig.builder("abenteuer-irland").description("Mum's website for Abenteuer Irland")
                    .homepage("https://arlol.github.io/abenteuer-irland/")
                    .githubPages(true)
                    .build(),
            RepositoryConfig.archived("actions"),
            RepositoryConfig.archived("actions-checkout-fetch-depth-demo"),
            RepositoryConfig.builder("advent-of-code").description("My advent of code solutions")
                    .build(),
            RepositoryConfig.archived("airmac"),
            RepositoryConfig.builder("angular-playground").description("A playground for the Angular framework")
                    .homepage("https://arlol.github.io/angular-playground/")
                    .githubPages(true)
                    .requiredStatusChecks("pr-check.required-status-check")
                    .build(),
            RepositoryConfig.builder("arlol.github.io").description("This is the source of my GitHub page")
                    .homepage("https://arlol.github.io/")
                    .githubPages(true)
                    .build(),
            RepositoryConfig.builder("beatunes-keytocomment")
                            .description("A beatunes plugin that writes the key to the comment")
                    .build(),
            RepositoryConfig.builder("bulma-playground").description("A playground for the Bulma CSS framework")
                    .homepage("https://arlol.github.io/bulma-playground/")
                    .githubPages(true)
                    .build(),
            RepositoryConfig.builder("business-english").description("Mum's website for Business English")
                    .homepage("https://arlol.github.io/business-english/")
                    .githubPages(true)
                    .build(),
            RepositoryConfig.builder("calver-tag-action")
                            .description("A GitHub Actions action that creates a new version using a CalVer-style derivative and pushes it")
                    .build(),
            RepositoryConfig.archived("campuswoche-2018-webseiten-steuern"),
            RepositoryConfig.archived("chop-kata"),
            RepositoryConfig.builder("chorito").description("A tool that does some chores in your source code")
                    .actionsSecrets("PAT")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("corporate-python")
                            .description("A container for executing python in corporate environments")
                    .build(),
            RepositoryConfig.builder("dependabot-dockerfile-test")
                            .description("A test to see whether dependabot updates dockerfiles with args")
                    .build(),
            RepositoryConfig.builder("dotfiles")
                            .description("My collection of dotfiles used to configure my command line environments")
                    .build(),
            RepositoryConfig.archived("dotnet-http-client-reproduction"),
            RepositoryConfig.builder("eclipse-projects").description("Arlo's project catalog for the Eclipse Installer")
                    .homepage("https://arlol.github.io/eclipse-projects/")
                    .githubPages(true)
                    .build(),
            RepositoryConfig.builder("effortful-retrieval-questions")
                            .description("A collection of effortful retrieval questions of a number of articles I've read")
                    .build(),
            RepositoryConfig.builder("git-dora-lead-time-calculator")
                            .description("A project to calculate the DORA metric lead time with the info from a git repo")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("git-presentation-2018-10")
                            .description("Git Präsentation für Vorlesung Industrielle Softwareentwicklung")
                    .build(),
            RepositoryConfig.archived("gitfx"),
            RepositoryConfig.archived("graalfx"),
            RepositoryConfig.archived("gwt-dragula-test"),
            RepositoryConfig.archived("gwt-log-print-style-demo"),
            RepositoryConfig.archived("gwt-refresh-demo"),
            RepositoryConfig.archived("HalloJSX"),
            RepositoryConfig.archived("HelloCocoaHTTPServer"),
            RepositoryConfig.archived("HelloIntAirActServer"),
            RepositoryConfig.archived("HelloRoutingServer"),
            RepositoryConfig.archived("HelloServer"),
            RepositoryConfig.builder("homebrew-tap").description("A homebrew tap for my own formulas and casks")
                    .build(),
            RepositoryConfig.archived("iebox"),
            RepositoryConfig.archived("ilabwebworkshop"),
            RepositoryConfig.archived("IntAirAct"),
            RepositoryConfig.archived("IntAirAct-Performance"),
            RepositoryConfig.archived("jBrowserDriver"),
            RepositoryConfig.archived("jbrowserdriver-cucumber-integration-tests"),
            RepositoryConfig.archived("jbrowserdriver-test"),
            RepositoryConfig.archived("jdk-newinstance-leak-demo"),
            RepositoryConfig.archived("jdk8u144-classloader-leak-demo-webapp"),
            RepositoryConfig.archived("jhipster-app"),
            RepositoryConfig.archived("json-smart-dependency-resolution-test"),
            RepositoryConfig.builder("kafka-debugger").description("A small jar utility to test kafka connections")
                    .build(),
            RepositoryConfig.archived("m2e-wro4j-bug-demo"),
            RepositoryConfig.archived("m2e-wro4j-bug-demo2"),
            RepositoryConfig.archived("maven-quickstart-j2objc"),
            RepositoryConfig.builder("menubar-scripts")
                            .description("A collection of scripts that can run in e.g. xbar, swiftbar, etc.")
                    .build(),
            RepositoryConfig.archived("Mirror"),
            RepositoryConfig.archived("modern-ie-vagrant"),
            RepositoryConfig.builder("music-stuff").description("Some spotify and beatunes stuff")
                    .requiredStatusChecks("test.required-status-check")
                    .build(),
            RepositoryConfig.builder("mvnx")
                            .description("An experiment with Maven dependencies and dynamic classloading")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.archived("MWPhotoBrowser"),
            RepositoryConfig.builder("myprojects-cleaner")
                            .description("A java application that runs git clean in a bunch of directories")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("newlinechecker")
                            .description("A sample project to play with GraalVM builds on GitHub Actions")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("nope-amine")
                            .description("A firefox extension that slowly increases the time for things to load on reddit.com")
                    .build(),
            RepositoryConfig.archived("npmrc-github-action"),
            RepositoryConfig.builder("open-webui-runner")
                            .description("A small repo to run open-webui locally and stop it after using it")
                    .build(),
            RepositoryConfig.archived("packer-templates"),
            RepositoryConfig.archived("pico-playground"),
            RepositoryConfig.archived("postgres-query-error-demo"),
            RepositoryConfig.builder("postgres-sync-demo")
                            .description("A demo on how to use triggers, queues, etc. to sync the app's data somewhere else")
                    .build(),
            RepositoryConfig.builder("python-nc")
                            .description("A test to see if I can implement nc's proxy functionality with python")
                    .build(),
            RepositoryConfig.archived("quickstart-buck-bazel-maven"),
            RepositoryConfig.builder("rss-to-mail")
                            .description("Read from RSS feeds and send an email for every new item")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("sci-fi-movies")
                            .description("an app to import sci fi movies from rotten tomatoes into a database in order to run queries on them")
                    .build(),
            RepositoryConfig.archived("selenium-xp-ie6"),
            RepositoryConfig.archived("self-hosted-gh-actions-runner"),
            RepositoryConfig.archived("spring-cloud-context-classloader-leak-demo"),
            RepositoryConfig.archived("spring-configuration-processor-metadata-bug-demo"),
            RepositoryConfig.archived("spring-security-drupal-password-encoder"),
            RepositoryConfig.builder("terraform-github")
                            .description("A project to manage github settings with terraform")
                    .environments(List.of(
                            new EnvironmentConfig("production", List.of("TF_GITHUB_TOKEN"))))
                    .build(),
            RepositoryConfig.archived("testcontainers-colima-github-actions"),
            RepositoryConfig.archived("toado"),
            RepositoryConfig.builder("tsaf-parser").description("Binary format exploration")
                    .requiredStatusChecks("test.required-status-check")
                    .build(),
            RepositoryConfig.archived("vagrant-1"),
            RepositoryConfig.builder("vagrant-ssh-config")
                            .description("A vagrant plugin that automatically creates ssh configs for vms")
                    .build(),
            RepositoryConfig.archived("vitest-link-reproduction"),
            RepositoryConfig.archived("vitest-mocking-reproduction"),
            RepositoryConfig.builder("wait-for-ports")
                            .description("A command-line utility that waits until a port is open")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("webapp-classloader-test")
                            .description("This is a test that can be used during integration testing to check for classloader leaks")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.builder("website-janitor")
                            .description("A set of tools that check websites for common misconfigurations or downtime")
                    .requiredStatusChecks("main.required-status-check")
                    .build(),
            RepositoryConfig.archived("workflow-dispatch-input-defaults")
    );

}
