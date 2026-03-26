package io.github.arlol.githubcheck.client;

import java.util.List;

public record RulesetResponse(
		Long id,
		String name,
		String target,
		String enforcement,
		Conditions conditions,
		List<Rule> rules
) {

	public record Conditions(
			RefName refName
	) {

		public record RefName(
				List<String> include,
				List<String> exclude
		) {
		}

	}

	public record Rule(
			String type,
			Parameters parameters
	) {

		public record Parameters(
				// required_status_checks rule fields
				List<StatusCheck> requiredStatusChecks,
				Boolean strictRequiredStatusChecksPolicy,
				// pull_request rule fields
				Integer requiredApprovingReviewCount,
				Boolean dismissStaleReviewsOnPush,
				Boolean requireCodeOwnerReview,
				Boolean requireLastPushApproval
		) {

			public record StatusCheck(
					String context,
					Integer integrationId
			) {
			}

		}

	}

}
