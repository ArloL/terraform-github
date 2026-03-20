package io.github.arlol.pulumigithub;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class StateImporter {

    public static void main(String[] args) throws Exception {
        var token = requireEnv("GITHUB_TOKEN");
        var owner = requireEnv("GITHUB_OWNER");
        validateAgainstGitHub(token, owner);

        var resources = buildResources();
        var tmpFile = Files.createTempFile("pulumi-import-", ".json");
        try {
            Files.writeString(tmpFile, toImportJson(resources));
            System.out.println("Importing " + resources.size() + " resources...");
            runPulumiImport(tmpFile);
        } finally {
            Files.deleteIfExists(tmpFile);
        }
    }

    static void validateAgainstGitHub(String token, String owner)
            throws IOException, InterruptedException {
        var ghNames = new TreeSet<>(new GitHubClient(token, owner).listRepositoryNames());
        var knownNames = Repositories.ALL.stream()
                .map(RepositoryConfig::name)
                .collect(Collectors.toSet());
        ghNames.removeAll(knownNames);
        if (!ghNames.isEmpty()) {
            throw new RuntimeException(
                    "Repositories exist on GitHub but are missing from Repositories.java:\n  "
                            + String.join("\n  ", ghNames));
        }
    }

    static String requireEnv(String name) {
        var value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Environment variable " + name + " is not set");
        }
        return value;
    }

    static JsonArray buildResources() {
        var resources = new JsonArray();
        for (var config : Repositories.ALL) {
            addResource(resources,
                    "github:index/repository:Repository",
                    config.name(), config.name());

            if (!config.archived()) {
                addResource(resources,
                        "github:index/branchDefault:BranchDefault",
                        config.name() + "-default", config.name());
            }

            if (!config.archived()) {
                addResource(resources,
                        "github:index/workflowRepositoryPermissions:WorkflowRepositoryPermissions",
                        config.name() + "-workflow-perms", config.name());

                addResource(resources,
                        "github:index/repositoryDependabotSecurityUpdates:RepositoryDependabotSecurityUpdates",
                        config.name() + "-dependabot", config.name());

                if (RepositoryProvisioner.shouldHaveBranchProtection(config)) {
                    addResource(resources,
                            "github:index/branchProtection:BranchProtection",
                            config.name() + "-protection", config.name() + ":main");
                }
            }

            if (!config.archived()) {
                for (var env : config.environments()) {
                    addResource(resources,
                            "github:index/repositoryEnvironment:RepositoryEnvironment",
                            config.name() + "-env-" + env.name(),
                            config.name() + ":" + env.name());
                }
            }
        }
        return resources;
    }

    static String toImportJson(JsonArray resources) {
        var root = new JsonObject();
        root.add("resources", resources);
        return new Gson().toJson(root);
    }

    private static void addResource(JsonArray resources, String type, String name, String id) {
        var obj = new JsonObject();
        obj.addProperty("type", type);
        obj.addProperty("name", name);
        obj.addProperty("id", id);
        resources.add(obj);
    }

    private static void runPulumiImport(Path file) throws IOException, InterruptedException {
        int exit = new ProcessBuilder(
                "pulumi", "import",
                "--file", file.toString(),
                "--yes",
                "--skip-preview")
                .inheritIO()
                .start()
                .waitFor();
        if (exit != 0) {
            throw new RuntimeException("pulumi import exited with code " + exit);
        }
    }

}
