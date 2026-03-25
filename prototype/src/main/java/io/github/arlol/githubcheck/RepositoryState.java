package io.github.arlol.githubcheck;

import java.util.List;
import java.util.Map;

import io.github.arlol.githubcheck.client.BranchProtection;
import io.github.arlol.githubcheck.client.RepositoryFull;
import io.github.arlol.githubcheck.client.RepositoryMinimal;
import io.github.arlol.githubcheck.client.WorkflowPermissions;

public record RepositoryState(
		String name,
		RepositoryMinimal summary,
		RepositoryFull details,
		boolean vulnerabilityAlerts,
		boolean automatedSecurityFixes,
		BranchProtection branchProtection,
		List<String> actionSecretNames,
		Map<String, List<String>> environmentSecretNames,
		WorkflowPermissions workflowPermissions
) {
}
