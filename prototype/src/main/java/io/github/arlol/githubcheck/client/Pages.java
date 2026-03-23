package io.github.arlol.githubcheck.client;

import com.fasterxml.jackson.annotation.JsonCreator;

public record Pages(
		String status,
		// Absent in legacy Pages responses that predate the build_type field.
		BuildType buildType
) {

	public enum BuildType {

		WORKFLOW, LEGACY;

		@JsonCreator
		public static BuildType fromValue(String value) {
			return valueOf(value.toUpperCase());
		}

	}

}
