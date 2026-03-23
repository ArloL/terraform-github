package io.github.arlol.githubcheck.client;

public record Pages(
		String status,
		// Absent in legacy Pages responses that predate the build_type field.
		BuildType buildType
) {
}
