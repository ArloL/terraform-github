package io.github.arlol.pulumigithub;

import com.pulumi.github.ActionsEnvironmentSecret;
import com.pulumi.github.ActionsEnvironmentSecretArgs;
import com.pulumi.github.ActionsSecret;
import com.pulumi.github.WorkflowRepositoryPermissions;
import com.pulumi.github.WorkflowRepositoryPermissionsArgs;
import com.pulumi.github.ActionsSecretArgs;
import com.pulumi.github.BranchDefault;
import com.pulumi.github.BranchDefaultArgs;
import com.pulumi.github.BranchProtection;
import com.pulumi.github.BranchProtectionArgs;
import com.pulumi.github.Repository;
import com.pulumi.github.RepositoryArgs;
import com.pulumi.github.RepositoryDependabotSecurityUpdates;
import com.pulumi.github.RepositoryDependabotSecurityUpdatesArgs;
import com.pulumi.github.RepositoryEnvironment;
import com.pulumi.github.RepositoryEnvironmentArgs;
import com.pulumi.github.inputs.BranchProtectionRequiredStatusCheckArgs;
import com.pulumi.github.inputs.RepositoryPagesArgs;
import com.pulumi.github.inputs.RepositorySecurityAndAnalysisArgs;
import com.pulumi.github.inputs.RepositorySecurityAndAnalysisSecretScanningArgs;
import com.pulumi.github.inputs.RepositorySecurityAndAnalysisSecretScanningPushProtectionArgs;
import com.pulumi.resources.CustomResourceOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositoryProvisioner {

    /**
     * Returns the map key used to look up a repo-level actions secret.
     * Matches the Terraform locals convention: "${repo.name}-${secret}".
     */
    public static String secretKey(String repoName, String secretName) {
        return repoName + "-" + secretName;
    }

    /**
     * Returns the map key used to look up an environment secret.
     * Matches the Terraform locals convention: "${repo.name}-${env.name}-${secret}".
     */
    public static String envSecretKey(String repoName, String envName, String secretName) {
        return repoName + "-" + envName + "-" + secretName;
    }

    /**
     * Returns true when branch protection should be created for this repo.
     * Only public repos get branch protection.
     */
    public static boolean shouldHaveBranchProtection(RepositoryConfig config) {
        return "public".equals(config.visibility());
    }

    /**
     * Builds the full set of required status check contexts for branch protection.
     * Always includes the four standard checks, then appends any repo-specific ones.
     */
    public static List<String> buildStatusChecks(RepositoryConfig config) {
        var checks = new ArrayList<>(List.of(
                "check-actions.required-status-check",
                "codeql-analysis.required-status-check",
                "CodeQL",
                "zizmor"
        ));
        checks.addAll(config.requiredStatusChecks());
        return checks;
    }

    /**
     * Creates all Pulumi GitHub resources for a single repository.
     * Resource name conventions:
     *   {name}                            — github_repository
     *   {name}-default                    — github_branch_default
     *   {name}-protection                 — github_branch_protection (conditional)
     *   {name}-dependabot                 — github_repository_dependabot_security_updates (conditional)
     *   {name}-env-{envName}              — github_repository_environment
     *   {name}-secret-{secretName}        — github_actions_secret
     *   {name}-envsecret-{env}-{secret}   — github_actions_environment_secret
     *   {name}-workflow-perms             — github_workflow_repository_permissions
     */
    public static void provision(RepositoryConfig config, Map<String, String> secrets) {
        // 1. github_repository
        var repoArgsBuilder = RepositoryArgs.builder()
                .name(config.name())
                .description(config.description())
                .visibility(config.visibility())
                .archived(config.archived())
                .allowMergeCommit(false)
                .allowSquashMerge(false)
                .deleteBranchOnMerge(true)
                .hasIssues(true)
                .hasProjects(true)
                .hasWiki(true)
                .allowAutoMerge(true)
                .autoInit(false)
                .vulnerabilityAlerts(true);

        if (config.homepage() != null) {
            repoArgsBuilder.homepageUrl(config.homepage());
        }

        if (config.githubPages()) {
            repoArgsBuilder.pages(RepositoryPagesArgs.builder()
                    .buildType("workflow")
                    .build());
        }

        repoArgsBuilder.securityAndAnalysis(RepositorySecurityAndAnalysisArgs.builder()
                .secretScanning(RepositorySecurityAndAnalysisSecretScanningArgs.builder()
                        .status("enabled")
                        .build())
                .secretScanningPushProtection(
                        RepositorySecurityAndAnalysisSecretScanningPushProtectionArgs.builder()
                                .status("enabled")
                                .build())
                .build());

        var repo = new Repository(config.name(), repoArgsBuilder.build());

        if (config.archived()) {
            return;
        }

        // 2. github_branch_default
        var branchDefault = new BranchDefault(config.name() + "-default",
                BranchDefaultArgs.builder()
                        .repository(repo.name())
                        .branch(config.mainBranch())
                        .build());

        // 3. github_branch_protection (public repos only)
        if (shouldHaveBranchProtection(config)) {
            new BranchProtection(config.name() + "-protection",
                    BranchProtectionArgs.builder()
                            .repositoryId(repo.nodeId())
                            .pattern(branchDefault.branch())
                            .enforceAdmins(true)
                            .requiredLinearHistory(true)
                            .requiredStatusChecks(BranchProtectionRequiredStatusCheckArgs.builder()
                                    .strict(false)
                                    .contexts(buildStatusChecks(config))
                                    .build())
                            .build());
        }

        // 4. github_repository_dependabot_security_updates
        new RepositoryDependabotSecurityUpdates(config.name() + "-dependabot",
                RepositoryDependabotSecurityUpdatesArgs.builder()
                        .repository(repo.id())
                        .enabled(true)
                        .build());

        // 5. github_repository_environment
        var createdEnvs = new HashMap<String, RepositoryEnvironment>();
        for (var env : config.environments()) {
            var repoEnv = new RepositoryEnvironment(
                    config.name() + "-env-" + env.name(),
                    RepositoryEnvironmentArgs.builder()
                            .repository(repo.name())
                            .environment(env.name())
                            .build());
            createdEnvs.put(env.name(), repoEnv);
        }

        // 6. github_actions_secret (repo-level)
        for (var secretName : config.actionsSecrets()) {
            var key = secretKey(config.name(), secretName);
            if (secrets.containsKey(key)) {
                new ActionsSecret(config.name() + "-secret-" + secretName,
                        ActionsSecretArgs.builder()
                                .repository(repo.id())
                                .secretName(secretName)
                                .plaintextValue(secrets.get(key))
                                .build());
            }
        }

        // 7. github_actions_environment_secret
        for (var envConfig : config.environments()) {
            var repoEnv = createdEnvs.get(envConfig.name());
            for (var secretName : envConfig.secrets()) {
                var key = envSecretKey(config.name(), envConfig.name(), secretName);
                if (secrets.containsKey(key)) {
                    new ActionsEnvironmentSecret(
                            config.name() + "-envsecret-" + envConfig.name() + "-" + secretName,
                            ActionsEnvironmentSecretArgs.builder()
                                    .repository(repo.id())
                                    .environment(envConfig.name())
                                    .secretName(secretName)
                                    .plaintextValue(secrets.get(key))
                                    .build(),
                            CustomResourceOptions.builder()
                                    .dependsOn(repoEnv)
                                    .build());
                }
            }
        }

        // 8. github_workflow_repository_permissions
        new WorkflowRepositoryPermissions(config.name() + "-workflow-perms",
                WorkflowRepositoryPermissionsArgs.builder()
                        .repository(repo.id())
                        .defaultWorkflowPermissions("read")
                        .canApprovePullRequestReviews(true)
                        .build());
    }

}
