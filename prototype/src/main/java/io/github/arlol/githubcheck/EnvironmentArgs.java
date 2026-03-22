package io.github.arlol.githubcheck;

import java.util.Arrays;
import java.util.List;

public final class EnvironmentArgs {

    private final List<String> secrets;

    private EnvironmentArgs(Builder builder) {
        this.secrets = List.copyOf(builder.secrets);
    }

    public List<String> secrets() {
        return secrets;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private List<String> secrets = List.of();

        public Builder secrets(String... secrets) {
            this.secrets = Arrays.asList(secrets);
            return this;
        }

        public EnvironmentArgs build() {
            return new EnvironmentArgs(this);
        }
    }
}
