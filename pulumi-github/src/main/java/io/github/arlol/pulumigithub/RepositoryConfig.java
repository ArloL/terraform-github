package io.github.arlol.pulumigithub;

import java.util.ArrayList;
import java.util.List;

public record RepositoryConfig(
        String name,
        String description,
        boolean archived,
        String homepage,
        boolean githubPages,
        String mainBranch,
        String visibility,
        List<String> requiredStatusChecks,
        List<String> actionsSecrets,
        List<EnvironmentConfig> environments
) {

    public static RepositoryConfig archived(String name) {
        return new Builder(name, "").archived(true).build();
    }

    public static Builder builder(String name) {
        return new Builder(name, "");
    }

    public static Builder builder(String name, String description) {
        return new Builder(name, description);
    }

    public static final class Builder {
        private final String name;
        private final String description;
        private boolean archived = false;
        private String homepage = null;
        private boolean githubPages = false;
        private String mainBranch = "main";
        private String visibility = "public";
        private List<String> requiredStatusChecks = List.of();
        private List<String> actionsSecrets = List.of();
        private List<EnvironmentConfig> environments = List.of();

        private Builder(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public Builder archived(boolean archived) {
            this.archived = archived;
            return this;
        }

        public Builder homepage(String homepage) {
            this.homepage = homepage;
            return this;
        }

        public Builder githubPages(boolean githubPages) {
            this.githubPages = githubPages;
            return this;
        }

        public Builder mainBranch(String mainBranch) {
            this.mainBranch = mainBranch;
            return this;
        }

        public Builder visibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder requiredStatusChecks(String... checks) {
            this.requiredStatusChecks = List.of(checks);
            return this;
        }

        public Builder actionsSecrets(String... secrets) {
            this.actionsSecrets = List.of(secrets);
            return this;
        }

        public Builder environments(List<EnvironmentConfig> environments) {
            this.environments = new ArrayList<>(environments);
            return this;
        }

        public RepositoryConfig build() {
            return new RepositoryConfig(
                    name, description, archived, homepage, githubPages,
                    mainBranch, visibility, requiredStatusChecks,
                    actionsSecrets, environments
            );
        }
    }

}
