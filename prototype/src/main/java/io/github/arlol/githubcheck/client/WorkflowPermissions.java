package io.github.arlol.githubcheck.client;

public record WorkflowPermissions(
		String defaultWorkflowPermissions,
		boolean canApprovePullRequestReviews
) {
}
