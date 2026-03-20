package io.github.arlol.pulumigithub;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.arlol.pulumigithub.RepositoryProvisioner.*;
import static org.junit.jupiter.api.Assertions.*;

class RepositoryProvisionerTest {

    private static RepositoryConfig publicActive(String name) {
        return RepositoryConfig.builder(name).description("description").build();
    }

    private static RepositoryConfig archived(String name) {
        return RepositoryConfig.builder(name).description("description").archived(true).build();
    }

    private static RepositoryConfig privateRepo(String name) {
        return RepositoryConfig.builder(name).description("description").visibility("private").build();
    }

    // --- shouldHaveBranchProtection ---

    @Test
    void branchProtectionForPublicActiveRepo() {
        assertTrue(shouldHaveBranchProtection(publicActive("my-repo")));
    }

    @Test
    void noBranchProtectionForPrivateRepo() {
        assertFalse(shouldHaveBranchProtection(privateRepo("my-repo")));
    }

    // --- buildStatusChecks ---

    @Test
    void statusChecksAlwaysContainFourDefaults() {
        var checks = buildStatusChecks(publicActive("my-repo"));
        assertTrue(checks.contains("check-actions.required-status-check"));
        assertTrue(checks.contains("codeql-analysis.required-status-check"));
        assertTrue(checks.contains("CodeQL"));
        assertTrue(checks.contains("zizmor"));
    }

    @Test
    void customStatusChecksAppendedAfterDefaults() {
        var config = RepositoryConfig.builder("my-repo").description("desc")
                .requiredStatusChecks("main.required-status-check")
                .build();
        var checks = buildStatusChecks(config);
        assertEquals(5, checks.size());
        assertTrue(checks.contains("main.required-status-check"));
        // defaults come first
        assertEquals("check-actions.required-status-check", checks.get(0));
    }

    @Test
    void noCustomChecksProducesFourChecks() {
        assertEquals(4, buildStatusChecks(publicActive("my-repo")).size());
    }

    // --- secretKey ---

    @Test
    void secretKeyMatchesTerraformConvention() {
        assertEquals("chorito-PAT", secretKey("chorito", "PAT"));
    }

    @Test
    void envSecretKeyMatchesTerraformConvention() {
        assertEquals("terraform-github-production-TF_GITHUB_TOKEN",
                envSecretKey("terraform-github", "production", "TF_GITHUB_TOKEN"));
    }

    // --- RepositoryConfig defaults ---

    @Test
    void defaultMainBranchIsMain() {
        assertEquals("main", publicActive("my-repo").mainBranch());
    }

    @Test
    void defaultVisibilityIsPublic() {
        assertEquals("public", publicActive("my-repo").visibility());
    }

    @Test
    void defaultArchivedIsFalse() {
        assertFalse(publicActive("my-repo").archived());
    }

    @Test
    void defaultGithubPagesIsFalse() {
        assertFalse(publicActive("my-repo").githubPages());
    }

    @Test
    void defaultEnvironmentsIsEmpty() {
        assertTrue(publicActive("my-repo").environments().isEmpty());
    }

    @Test
    void defaultActionsSecretsIsEmpty() {
        assertTrue(publicActive("my-repo").actionsSecrets().isEmpty());
    }

    // --- EnvironmentConfig ---

    @Test
    void environmentConfigWithOneArgHasNoSecrets() {
        var env = new EnvironmentConfig("production");
        assertEquals("production", env.name());
        assertTrue(env.secrets().isEmpty());
    }

    @Test
    void environmentConfigWithSecrets() {
        var env = new EnvironmentConfig("production", List.of("TF_GITHUB_TOKEN"));
        assertEquals(List.of("TF_GITHUB_TOKEN"), env.secrets());
    }

}
