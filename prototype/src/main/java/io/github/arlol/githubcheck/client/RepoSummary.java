package io.github.arlol.githubcheck.client;

public record RepoSummary(
		String name,
		boolean archived,
		String visibility
) {
}
