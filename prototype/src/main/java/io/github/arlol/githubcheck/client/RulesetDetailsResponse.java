package io.github.arlol.githubcheck.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RulesetDetailsResponse(
		long id,
		String name,
		String target,
		String enforcement,
		String nodeId,
		SourceType sourceType,
		String source,
		CurrentUserCanBypass currentUserCanBypass,
		String createdAt,
		String updatedAt,
		List<BypassActor> bypassActors,
		Conditions conditions,
		List<Rule> rules
) {

	public enum SourceType {
		@JsonProperty("Repository")
		REPOSITORY, @JsonProperty("Organization")
		ORGANIZATION
	}

	public enum CurrentUserCanBypass {
		@JsonProperty("always")
		ALWAYS, @JsonProperty("pull_requests_only")
		PULL_REQUESTS_ONLY, @JsonProperty("never")
		NEVER
	}

	public record BypassActor(
			Long actorId,
			ActorType actorType,
			BypassMode bypassMode
	) {

		public enum ActorType {
			@JsonProperty("Integration")
			INTEGRATION, @JsonProperty("OrganizationAdmin")
			ORGANIZATION_ADMIN, @JsonProperty("RepositoryRole")
			REPOSITORY_ROLE, @JsonProperty("Team")
			TEAM
		}

		public enum BypassMode {
			@JsonProperty("always")
			ALWAYS, @JsonProperty("pull_request")
			PULL_REQUEST
		}

	}

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

			public record PropertyCondition(
					String name,
					List<String> propertyValues,
					String source
			) {
			}

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
