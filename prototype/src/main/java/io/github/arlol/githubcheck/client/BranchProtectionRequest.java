package io.github.arlol.githubcheck.client;

import java.util.List;

public record BranchProtectionRequest(
		RequiredStatusChecks requiredStatusChecks,
		boolean enforceAdmins,
		RequiredPullRequestReviews requiredPullRequestReviews,
		Restrictions restrictions,
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

	public record RequiredPullRequestReviews(
			boolean dismissStaleReviews,
			boolean requireCodeOwnerReviews,
			Integer requiredApprovingReviewCount,
			Boolean requireLastPushApproval
	) {
	}

	// The write API identifies users/teams/apps by login/slug strings,
	// not by the full objects returned in the response.
	public record Restrictions(
			List<String> users,
			List<String> teams,
			List<String> apps
	) {
	}

}
