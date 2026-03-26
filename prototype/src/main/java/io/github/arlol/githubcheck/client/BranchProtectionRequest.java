package io.github.arlol.githubcheck.client;

import java.util.List;

public record BranchProtectionRequest(
		RequiredStatusChecks requiredStatusChecks,
		boolean enforceAdmins,
		Object requiredPullRequestReviews,
		Object restrictions,
		boolean requiredLinearHistory,
		boolean allowForcePushes
) {

	public record RequiredStatusChecks(
			boolean strict,
			List<StatusCheck> checks
	) {

		public record StatusCheck(
				String context
		) {
		}

	}

}
