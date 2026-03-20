package io.github.arlol.pulumigithub;

import java.util.List;

public record EnvironmentConfig(String name, List<String> secrets) {

    public EnvironmentConfig(String name) {
        this(name, List.of());
    }

}
