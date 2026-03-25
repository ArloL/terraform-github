package io.github.arlol.githubcheck;

import java.util.List;
import java.util.Map;

import io.github.arlol.githubcheck.client.WorkflowPermissions;

public record RepositoryState(
		String name,
		boolean archived,
		String visibility,
		String description,
		String homepageUrl,
		boolean hasIssues,
		boolean hasProjects,
		boolean hasWiki,
		String defaultBranch,
		List<String> topics,
		boolean allowMergeCommit,
		boolean allowSquashMerge,
		boolean allowAutoMerge,
		boolean deleteBranchOnMerge,
		boolean vulnerabilityAlerts,
		boolean automatedSecurityFixes,
		boolean secretScanning,
		boolean secretScanningPushProtection,
		boolean branchProtectionExists,
		boolean enforceAdmins,
		boolean requiredLinearHistory,
		boolean allowForcePushes,
		boolean requiredStatusChecksStrict,
		List<String> requiredStatusCheckContexts,
		List<String> actionSecretNames,
		Map<String, List<String>> environmentSecretNames,
		WorkflowPermissions workflowPermissions
) {
}
