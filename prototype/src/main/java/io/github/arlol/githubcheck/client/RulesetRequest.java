package io.github.arlol.githubcheck.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RulesetRequest(
		String name,
		String target,
		String enforcement,
		Conditions conditions,
		List<Rule> rules
) {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Conditions(
			RefName refName,
			RepositoryName repositoryName,
			RepositoryId repositoryId,
			RepositoryProperty repositoryProperty
	) {

		public record RefName(
				List<String> include,
				List<String> exclude
		) {
		}

		@JsonInclude(JsonInclude.Include.NON_NULL)
		public record RepositoryName(
				List<String> include,
				List<String> exclude,
				@JsonProperty("protected") Boolean isProtected
		) {
		}

		public record RepositoryId(
				List<Long> repositoryIds
		) {
		}

		public record RepositoryProperty(
				List<PropertyCondition> include,
				List<PropertyCondition> exclude
		) {

			@JsonInclude(JsonInclude.Include.NON_NULL)
			public record PropertyCondition(
					String name,
					List<String> propertyValues,
					String source
			) {
			}

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
