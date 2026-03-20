package io.github.arlol.pulumigithub;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pulumi.Pulumi;

import java.util.Map;
import java.util.Objects;

public class App {

    public static void main(String[] args) {
        Pulumi.run(App::stack);
    }

    static void stack(com.pulumi.Context ctx) {
        // Reuse the same env var as Terraform for easy side-by-side comparison.
        // Format: JSON map of {"repoName-secretName": "value"} for repo-level secrets
        // and {"repoName-envName-secretName": "value"} for environment secrets.
        var secretValuesJson = Objects.requireNonNullElse(
                System.getenv("TF_VAR_secret_values"), "{}");
        Map<String, String> secrets = Objects.requireNonNullElse(
                new Gson().fromJson(secretValuesJson,
                        new TypeToken<Map<String, String>>() {}.getType()),
                Map.of());

        for (var config : Repositories.ALL) {
            RepositoryProvisioner.provision(config, secrets);
        }
    }

}
