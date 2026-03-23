package io.github.arlol.githubcheck.client;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum BuildType {

	WORKFLOW, LEGACY;

	@JsonCreator
	public static BuildType fromValue(String value) {
		return valueOf(value.toUpperCase());
	}

}
