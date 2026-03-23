package io.github.arlol.githubcheck.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RepoDetails(
		long id,
		String nodeId,
		String name,
		String fullName,
		@JsonProperty("private") boolean isPrivate,
		boolean fork,
		boolean archived,
		boolean disabled,
		boolean isTemplate,
		String visibility,
		String defaultBranch,
		String description, // nullable — GitHub returns null for unset
		String homepage, // nullable — GitHub returns null for unset
		List<String> topics,
		boolean hasIssues,
		boolean hasProjects,
		boolean hasWiki,
		boolean hasDiscussions,
		boolean hasPages,
		boolean allowForking,
		boolean webCommitSignoffRequired,
		boolean allowSquashMerge,
		boolean allowMergeCommit,
		boolean allowRebaseMerge,
		boolean allowAutoMerge,
		boolean deleteBranchOnMerge,
		boolean allowUpdateBranch,
		String squashMergeCommitTitle,
		String squashMergeCommitMessage,
		String mergeCommitTitle,
		String mergeCommitMessage,
		// May be absent for archived repos or repos where security features
		// are not available (e.g. private repos without GHAS).
		SecurityAndAnalysis securityAndAnalysis
) {
}
