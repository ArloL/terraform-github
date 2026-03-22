package io.github.arlol.githubcheck;

public final class Repository {

    private final String name;
    private final RepositoryArgs args;

    public Repository(String name) {
        this(name, RepositoryArgs.builder().build());
    }

    public Repository(String name, RepositoryArgs args) {
        this.name = name;
        this.args = args;
    }

    public String name() {
        return name;
    }

    public RepositoryArgs args() {
        return args;
    }
}
