package io.github.arlol.pulumigithub;

import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RepositoriesTest {

    @Test
    void allReposHaveNonEmptyName() {
        Repositories.ALL.forEach(r ->
                assertFalse(r.name().isBlank(), "Repository has blank name: " + r));
    }

    @Test
    void allReposHaveNonEmptyDescription() {
        Repositories.ALL.stream()
                .filter(r -> !r.archived())
                .forEach(r ->
                        assertFalse(r.description().isBlank(),
                                "Repository has blank description: " + r.name()));
    }

    @Test
    void repoNamesAreUnique() {
        var names = Repositories.ALL.stream().map(RepositoryConfig::name).toList();
        var duplicates = names.stream()
                .filter(name -> names.stream().filter(name::equals).count() > 1)
                .collect(Collectors.toSet());
        assertTrue(duplicates.isEmpty(), "Duplicate repo names: " + duplicates);
    }

    @Test
    void repoCountMatchesTerraform() {
        assertEquals(84, Repositories.ALL.size());
    }

    @Test
    void allReposHaveDefaultVisibility() {
        Repositories.ALL.forEach(r ->
                assertNotNull(r.visibility(), "Visibility is null for: " + r.name()));
    }

    @Test
    void allReposHaveDefaultMainBranch() {
        Repositories.ALL.forEach(r ->
                assertFalse(r.mainBranch().isBlank(),
                        "mainBranch is blank for: " + r.name()));
    }

    @Test
    void archivedReposHaveNoRequiredStatusChecks() {
        Repositories.ALL.stream()
                .filter(RepositoryConfig::archived)
                .forEach(r -> assertTrue(r.requiredStatusChecks().isEmpty(),
                        "Archived repo has required status checks: " + r.name()));
    }

    @Test
    void terraformGithubHasProductionEnvironment() {
        var tfGithub = Repositories.ALL.stream()
                .filter(r -> "terraform-github".equals(r.name()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("terraform-github not found"));
        assertEquals(1, tfGithub.environments().size());
        assertEquals("production", tfGithub.environments().getFirst().name());
        assertTrue(tfGithub.environments().getFirst().secrets().contains("TF_GITHUB_TOKEN"));
    }

    @Test
    void choritoHasPATSecret() {
        var chorito = Repositories.ALL.stream()
                .filter(r -> "chorito".equals(r.name()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("chorito not found"));
        assertTrue(chorito.actionsSecrets().contains("PAT"));
    }

}
