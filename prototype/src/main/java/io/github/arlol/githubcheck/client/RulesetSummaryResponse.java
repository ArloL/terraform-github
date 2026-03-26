package io.github.arlol.githubcheck.client;

public record RulesetSummaryResponse(
		long id,
		String name,
		String enforcement,
		String nodeId,
		RulesetDetailsResponse.SourceType sourceType,
		String source,
		String createdAt,
		String updatedAt
) {
}
