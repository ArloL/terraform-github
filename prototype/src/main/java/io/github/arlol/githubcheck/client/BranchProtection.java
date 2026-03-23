package io.github.arlol.githubcheck.client;

import java.util.List;

public record BranchProtection(
		EnforceAdmins enforceAdmins,
		RequiredLinearHistory requiredLinearHistory,
		AllowForcePushes allowForcePushes,
		// Absent when no status-check rules are configured.
		RequiredStatusChecks requiredStatusChecks
) {

	public record EnforceAdmins(
			boolean enabled
	) {
	}

	public record RequiredLinearHistory(
			boolean enabled
	) {
	}

	public record AllowForcePushes(
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
