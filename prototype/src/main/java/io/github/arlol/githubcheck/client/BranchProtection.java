package io.github.arlol.githubcheck.client;

import java.util.List;

public record BranchProtection(
		Enabled enforceAdmins,
		Enabled requiredLinearHistory,
		Enabled allowForcePushes,
		// Absent when no status-check rules are configured.
		RequiredStatusChecks requiredStatusChecks
) {

	public record Enabled(
			boolean enabled
	) {
	}

	public record RequiredStatusChecks(
			boolean strict,
			// Modern API returns checks[].context; legacy returns contexts[].
			List<StatusCheck> checks,
			List<String> contexts
	) {

		public record StatusCheck(
				String context
		) {
		}

	}

}
