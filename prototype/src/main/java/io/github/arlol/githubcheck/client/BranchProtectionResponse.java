package io.github.arlol.githubcheck.client;

import java.util.List;

public record BranchProtectionResponse(
		EnforceAdmins enforceAdmins,
		RequiredLinearHistory requiredLinearHistory,
		AllowForcePushes allowForcePushes,
		AllowDeletions allowDeletions, // optional
		BlockCreations blockCreations, // optional
		RequiredConversationResolution requiredConversationResolution, // optional
		// Absent when no status-check rules are configured.
		RequiredStatusChecks requiredStatusChecks,
		RequiredPullRequestReviews requiredPullRequestReviews, // optional
		Restrictions restrictions, // optional
		RequiredSignatures requiredSignatures, // optional
		LockBranch lockBranch, // optional
		AllowForkSyncing allowForkSyncing // optional
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

	public record AllowDeletions(
			boolean enabled
	) {
	}

	public record BlockCreations(
			boolean enabled
	) {
	}

	public record RequiredConversationResolution(
			boolean enabled
	) {
	}

	public record RequiredSignatures(
			boolean enabled
	) {
	}

	public record LockBranch(
			Boolean enabled // optional, default false
	) {
	}

	public record AllowForkSyncing(
			Boolean enabled // optional, default false
	) {
	}

	public record RequiredStatusChecks(
			boolean strict,
			// Modern API returns checks[].context; legacy returns contexts[].
			List<StatusCheck> checks,
			List<String> contexts
	) {

		public record StatusCheck(
				String context,
				Integer appId // nullable
		) {
		}

	}

	public record RequiredPullRequestReviews(
			boolean dismissStaleReviews,
			boolean requireCodeOwnerReviews,
			Integer requiredApprovingReviewCount, // optional
			Boolean requireLastPushApproval // optional, default false
	) {
	}

	public record Restrictions(
			List<SimpleUser> users,
			List<Team> teams,
			List<App> apps
	) {

		public record Team(
				Long id,
				String nodeId,
				String name,
				String slug,
				String permission
		) {
		}

		public record App(
				Long id,
				String nodeId,
				String slug,
				String name,
				String clientId,
				String description,
				String externalUrl,
				String htmlUrl,
				String createdAt,
				String updatedAt
		) {
		}

	}

}
