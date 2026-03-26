package io.github.arlol.githubcheck.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public record RulesetRequest(
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

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Rule(
			String type,
			Parameters parameters
	) {

		@JsonInclude(JsonInclude.Include.NON_NULL)
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

			@JsonInclude(JsonInclude.Include.NON_NULL)
			public record StatusCheck(
					String context,
					Integer integrationId
			) {
			}

		}

	}

}
