package io.github.arlol.githubcheck.client;

public record BranchProtection(
		EnabledObject enforceAdmins,
		EnabledObject requiredLinearHistory,
		EnabledObject allowForcePushes,
		// Absent when no status-check rules are configured.
		RequiredStatusChecks requiredStatusChecks
) {
}
