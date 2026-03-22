package io.github.arlol.githubcheck;

import java.util.List;
import java.util.Map;

public record RepositoryState(
		String name,
		boolean archived,
		String visibility,
		String description,
		String homepageUrl,
		boolean allowMergeCommit,
		boolean allowSquashMerge,
		boolean allowAutoMerge,
		boolean deleteBranchOnMerge,
		boolean vulnerabilityAlerts,
		boolean secretScanning,
		boolean secretScanningPushProtection,
		boolean branchProtectionExists,
		boolean enforceAdmins,
		boolean requiredLinearHistory,
		List<String> requiredStatusCheckContexts,
		List<String> actionSecretNames,
		Map<String, List<String>> environmentSecretNames,
		String workflowPermissionsDefault,
		boolean canApprovePullRequestReviews
) {
}
