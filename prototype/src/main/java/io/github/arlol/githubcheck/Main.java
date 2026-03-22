package io.github.arlol.githubcheck;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        String token = System.getenv("GITHUB_TOKEN");
        if (token == null || token.isBlank()) {
            System.err.println("ERROR: GITHUB_TOKEN environment variable not set");
            System.exit(1);
        }

        long startTime = System.currentTimeMillis();

        var checker = new OrgChecker(token, "ArloL");
        CheckResult result = checker.check(repositories());
        checker.printReport(result);

        double totalSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.printf("%nTotal execution time: %.2f seconds%n", totalSeconds);

        System.exit(result.hasDrift() ? 1 : 0);
    }

    static List<Repository> repositories() {
        return List.of(
                new Repository("abenteuer-irland"),
                new Repository("actions", RepositoryArgs.builder()
                        .archived()
                        .build()),
                new Repository("advent-of-code"),
                new Repository("angular-playground", RepositoryArgs.builder()
                        .requiredStatusChecks("pr-check.required-status-check")
                        .build()),
                new Repository("arlol.github.io"),
                new Repository("beatunes-keytocomment"),
                new Repository("bulma-playground"),
                new Repository("business-english"),
                new Repository("calver-tag-action"),
                new Repository("chorito", RepositoryArgs.builder()
                        .requiredStatusChecks("main.required-status-check")
                        .actionSecrets("PAT")
                        .build()),
                new Repository("corporate-python"),
                new Repository("dependabot-dockerfile-test"),
                new Repository("dotfiles"),
                new Repository("dotnet-http-client-reproduction", RepositoryArgs.builder()
                        .archived()
                        .build()),
                new Repository("eclipse-projects"),
                new Repository("effortful-retrieval-questions"),
                new Repository("git-dora-lead-time-calculator", RepositoryArgs.builder()
                        .requiredStatusChecks("main.required-status-check")
                        .build()),
                new Repository("git-presentation-2018-10"),
                new Repository("gitfx", RepositoryArgs.builder()
                        .archived()
                        .build()),
                new Repository("graalfx", RepositoryArgs.builder()
                        .archived()
                        .build()),
                new Repository("homebrew-tap"),
                new Repository("kafka-debugger"),
                new Repository("menubar-scripts"),
                new Repository("music-stuff", RepositoryArgs.builder()
                        .requiredStatusChecks("test.required-status-check")
                        .build()),
                new Repository("mvnx", RepositoryArgs.builder()
                        .requiredStatusChecks("main.required-status-check")
                        .build()),
                new Repository("myprojects-cleaner", RepositoryArgs.builder()
                        .requiredStatusChecks("main.required-status-check")
                        .build()),
                new Repository("newlinechecker", RepositoryArgs.builder()
                        .requiredStatusChecks("main.required-status-check")
                        .build()),
                new Repository("nope-amine"),
                new Repository("open-webui-runner"),
                new Repository("pico-playground", RepositoryArgs.builder()
                        .archived()
                        .build()),
                new Repository("postgres-sync-demo"),
                new Repository("python-nc"),
                new Repository("rss-to-mail", RepositoryArgs.builder()
                        .requiredStatusChecks("main.required-status-check")
                        .build()),
                new Repository("sci-fi-movies"),
                new Repository("self-hosted-gh-actions-runner", RepositoryArgs.builder()
                        .archived()
                        .build()),
                new Repository("terraform-github", RepositoryArgs.builder()
                        .environment("production", env -> env.secrets("TF_GITHUB_TOKEN"))
                        .build()),
                new Repository("testcontainers-colima-github-actions", RepositoryArgs.builder()
                        .archived()
                        .build()),
                new Repository("tsaf-parser", RepositoryArgs.builder()
                        .requiredStatusChecks("test.required-status-check")
                        .build()),
                new Repository("vagrant-ssh-config"),
                new Repository("vitest-link-reproduction", RepositoryArgs.builder()
                        .archived()
                        .build()),
                new Repository("wait-for-ports", RepositoryArgs.builder()
                        .requiredStatusChecks("main.required-status-check")
                        .build()),
                new Repository("webapp-classloader-test", RepositoryArgs.builder()
                        .requiredStatusChecks("main.required-status-check")
                        .build()),
                new Repository("website-janitor", RepositoryArgs.builder()
                        .requiredStatusChecks("main.required-status-check")
                        .build()));
    }
}
