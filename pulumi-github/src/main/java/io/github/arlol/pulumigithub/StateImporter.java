package io.github.arlol.pulumigithub;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StateImporter {

    public static void main(String[] args) throws Exception {
        var json = buildImportJson();
        var tmpFile = Files.createTempFile("pulumi-import-", ".json");
        try {
            Files.writeString(tmpFile, json);
            System.out.println("Importing " + countResources(json) + " resources...");
            runPulumiImport(tmpFile);
        } finally {
            Files.deleteIfExists(tmpFile);
        }
    }

    static String buildImportJson() {
        var resources = new JsonArray();
        for (var config : Repositories.ALL) {
            addResource(resources,
                    "github:index/repository:Repository",
                    config.name(), config.name());

            addResource(resources,
                    "github:index/branchDefault:BranchDefault",
                    config.name() + "-default", config.name());

            addResource(resources,
                    "github:index/workflowRepositoryPermissions:WorkflowRepositoryPermissions",
                    config.name() + "-workflow-perms", config.name());

            if (!config.archived()) {
                addResource(resources,
                        "github:index/repositoryDependabotSecurityUpdates:RepositoryDependabotSecurityUpdates",
                        config.name() + "-dependabot", config.name());

                if (RepositoryProvisioner.shouldHaveBranchProtection(config)) {
                    addResource(resources,
                            "github:index/branchProtection:BranchProtection",
                            config.name() + "-protection", config.name() + ":main");
                }
            }

            for (var env : config.environments()) {
                addResource(resources,
                        "github:index/repositoryEnvironment:RepositoryEnvironment",
                        config.name() + "-env-" + env.name(),
                        config.name() + ":" + env.name());
            }
        }
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

    private static int countResources(String json) {
        int count = 0;
        int idx = 0;
        while ((idx = json.indexOf("\"type\"", idx)) != -1) {
            count++;
            idx++;
        }
        return count;
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
