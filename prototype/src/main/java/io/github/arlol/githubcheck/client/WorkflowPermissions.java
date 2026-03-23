package io.github.arlol.githubcheck.client;

import com.fasterxml.jackson.annotation.JsonCreator;

public record WorkflowPermissions(
		DefaultWorkflowPermissions defaultWorkflowPermissions,
		boolean canApprovePullRequestReviews
) {

	public enum DefaultWorkflowPermissions {

		READ, WRITE;

		@JsonCreator
		public static DefaultWorkflowPermissions fromValue(String value) {
			return valueOf(value.toUpperCase());
		}

	}

}
