package io.github.arlol.githubcheck.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record EnvironmentDetailsResponse(
		String name,
		List<ProtectionRule> protectionRules,
		DeploymentBranchPolicy deploymentBranchPolicy
) {

	public EnvironmentDetailsResponse {
		protectionRules = protectionRules != null ? List.copyOf(protectionRules)
				: List.of();
	}

	public record ProtectionRule(
			String type,
			Integer waitTimer,
			List<Reviewer> reviewers
	) {

		public ProtectionRule {
			reviewers = reviewers != null ? List.copyOf(reviewers) : List.of();
		}

	}

	public record Reviewer(
			String type,
			ReviewerEntity reviewer
	) {
	}

	public record ReviewerEntity(
			Long id,
			String login,
			String slug
	) {
	}

	public record DeploymentBranchPolicy(
			boolean protectedBranches,
			boolean customBranchPolicies
	) {
	}

	public Integer getWaitTimer() {
		if (protectionRules == null) {
			return null;
		}
		return protectionRules.stream()
				.filter(r -> "wait_timer".equals(r.type()))
				.findFirst()
				.map(ProtectionRule::waitTimer)
				.orElse(null);
	}

	public Set<String> getReviewerIds() {
		Set<String> ids = new HashSet<>();
		if (protectionRules == null) {
			return ids;
		}
		for (ProtectionRule rule : protectionRules) {
			if ("required_reviewers".equals(rule.type())
					&& rule.reviewers() != null) {
				for (Reviewer r : rule.reviewers()) {
					if (r.reviewer() != null && r.reviewer().id() != null) {
						ids.add(r.type() + ":" + r.reviewer().id());
					}
				}
			}
		}
		return ids;
	}

}
