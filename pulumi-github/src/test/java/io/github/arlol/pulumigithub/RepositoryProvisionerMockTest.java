package io.github.arlol.pulumigithub;

import com.pulumi.test.Mocks;
import com.pulumi.test.PulumiTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryProvisionerMockTest {

    @AfterEach
    void cleanup() {
        PulumiTest.cleanup();
    }

    private static Mocks capturingMocks(List<String> capturedTypes) {
        return args -> {
            capturedTypes.add(args.type);
            var state = new HashMap<>(args.inputs);
            // BranchProtection uses repo.nodeId() as repositoryId — computed output not in inputs
            state.put("nodeId", args.name + "-node-id");
            return CompletableFuture.completedFuture(
                    Mocks.ResourceResult.of(Optional.of(args.name), state));
        };
    }

    @Test
    void publicActiveRepoCreatesAllResources() throws Exception {
        var config = RepositoryConfig.builder("my-repo").description("A public active repo").build();
        var capturedTypes = new ArrayList<String>();

        PulumiTest.withMocks(capturingMocks(capturedTypes))
                .runTest(ctx -> RepositoryProvisioner.provision(config, Map.of()))
                .throwOnError();

        assertTrue(capturedTypes.contains("github:index/repository:Repository"));
        assertTrue(capturedTypes.contains("github:index/branchDefault:BranchDefault"));
        assertTrue(capturedTypes.contains("github:index/branchProtection:BranchProtection"));
        assertTrue(capturedTypes.contains(
                "github:index/repositoryDependabotSecurityUpdates:RepositoryDependabotSecurityUpdates"));
        assertTrue(capturedTypes.contains(
                "github:index/workflowRepositoryPermissions:WorkflowRepositoryPermissions"));
    }

    @Test
    void archivedRepoSkipsBranchProtectionAndDependabot() throws Exception {
        var config = RepositoryConfig.builder("old-repo").description("An archived repo")
                .archived(true)
                .build();
        var capturedTypes = new ArrayList<String>();

        PulumiTest.withMocks(capturingMocks(capturedTypes))
                .runTest(ctx -> RepositoryProvisioner.provision(config, Map.of()))
                .throwOnError();

        assertTrue(capturedTypes.contains("github:index/repository:Repository"));
        assertFalse(capturedTypes.contains("github:index/branchDefault:BranchDefault"),
                "archived repo should not have branch default");
        assertFalse(capturedTypes.contains("github:index/branchProtection:BranchProtection"),
                "archived repo should not have branch protection");
        assertFalse(capturedTypes.contains(
                "github:index/repositoryDependabotSecurityUpdates:RepositoryDependabotSecurityUpdates"),
                "archived repo should not have dependabot security updates");
        assertFalse(capturedTypes.contains(
                "github:index/workflowRepositoryPermissions:WorkflowRepositoryPermissions"),
                "archived repo should not have workflow permissions");
    }

    @Test
    void privateRepoSkipsBranchProtectionOnly() throws Exception {
        var config = RepositoryConfig.builder("private-repo").description("A private active repo")
                .visibility("private")
                .build();
        var capturedTypes = new ArrayList<String>();

        PulumiTest.withMocks(capturingMocks(capturedTypes))
                .runTest(ctx -> RepositoryProvisioner.provision(config, Map.of()))
                .throwOnError();

        assertTrue(capturedTypes.contains("github:index/repository:Repository"));
        assertFalse(capturedTypes.contains("github:index/branchProtection:BranchProtection"),
                "private repo should not have branch protection");
        assertTrue(capturedTypes.contains(
                "github:index/repositoryDependabotSecurityUpdates:RepositoryDependabotSecurityUpdates"),
                "private non-archived repo should still have dependabot security updates");
    }

    @Test
    void environmentsCreateRepositoryEnvironmentResources() throws Exception {
        var config = RepositoryConfig.builder("env-repo").description("Repo with environments")
                .environments(List.of(
                        new EnvironmentConfig("staging"),
                        new EnvironmentConfig("production")))
                .build();
        var capturedTypes = new ArrayList<String>();

        PulumiTest.withMocks(capturingMocks(capturedTypes))
                .runTest(ctx -> RepositoryProvisioner.provision(config, Map.of()))
                .throwOnError();

        long envCount = capturedTypes.stream()
                .filter("github:index/repositoryEnvironment:RepositoryEnvironment"::equals)
                .count();
        assertEquals(2, envCount, "should create one RepositoryEnvironment per environment");
    }

    @Test
    void actionsSecretCreatedOnlyWhenKeyPresent() throws Exception {
        var config = RepositoryConfig.builder("secret-repo").description("Repo with secrets")
                .actionsSecrets("PAT", "OTHER")
                .build();
        var capturedTypes = new ArrayList<String>();

        // Only provide key for PAT, not OTHER
        var secrets = Map.of("secret-repo-PAT", "token-value");

        PulumiTest.withMocks(capturingMocks(capturedTypes))
                .runTest(ctx -> RepositoryProvisioner.provision(config, secrets))
                .throwOnError();

        long secretCount = capturedTypes.stream()
                .filter("github:index/actionsSecret:ActionsSecret"::equals)
                .count();
        assertEquals(1, secretCount, "should create ActionsSecret only for keys present in secrets map");
    }

    @Test
    void actionsSecretNotCreatedWhenSecretsMapEmpty() throws Exception {
        var config = RepositoryConfig.builder("secret-repo").description("Repo with secrets")
                .actionsSecrets("PAT")
                .build();
        var capturedTypes = new ArrayList<String>();

        PulumiTest.withMocks(capturingMocks(capturedTypes))
                .runTest(ctx -> RepositoryProvisioner.provision(config, Map.of()))
                .throwOnError();

        assertFalse(capturedTypes.contains("github:index/actionsSecret:ActionsSecret"),
                "should not create ActionsSecret when key is absent from secrets map");
    }

    @Test
    void envSecretCreatedOnlyWhenKeyPresent() throws Exception {
        var envWithSecret = new EnvironmentConfig("production", List.of("TF_GITHUB_TOKEN"));
        var config = RepositoryConfig.builder("env-secret-repo").description("Repo with env secrets")
                .environments(List.of(envWithSecret))
                .build();
        var capturedTypes = new ArrayList<String>();

        var secrets = Map.of("env-secret-repo-production-TF_GITHUB_TOKEN", "gh-token");

        PulumiTest.withMocks(capturingMocks(capturedTypes))
                .runTest(ctx -> RepositoryProvisioner.provision(config, secrets))
                .throwOnError();

        assertTrue(capturedTypes.contains("github:index/actionsEnvironmentSecret:ActionsEnvironmentSecret"),
                "should create ActionsEnvironmentSecret when env secret key is present");
    }

    @Test
    void envSecretNotCreatedWhenKeyAbsent() throws Exception {
        var envWithSecret = new EnvironmentConfig("production", List.of("TF_GITHUB_TOKEN"));
        var config = RepositoryConfig.builder("env-secret-repo").description("Repo with env secrets")
                .environments(List.of(envWithSecret))
                .build();
        var capturedTypes = new ArrayList<String>();

        PulumiTest.withMocks(capturingMocks(capturedTypes))
                .runTest(ctx -> RepositoryProvisioner.provision(config, Map.of()))
                .throwOnError();

        assertFalse(capturedTypes.contains("github:index/actionsEnvironmentSecret:ActionsEnvironmentSecret"),
                "should not create ActionsEnvironmentSecret when env secret key is absent");
    }

    @Test
    void repoWithNoEnvironmentsCreatesNoEnvironmentResources() throws Exception {
        var config = RepositoryConfig.builder("plain-repo").description("Plain repo").build();
        var capturedTypes = new ArrayList<String>();

        PulumiTest.withMocks(capturingMocks(capturedTypes))
                .runTest(ctx -> RepositoryProvisioner.provision(config, Map.of()))
                .throwOnError();

        assertFalse(capturedTypes.contains("github:index/repositoryEnvironment:RepositoryEnvironment"),
                "repo with no environments should create no RepositoryEnvironment resources");
    }
}
