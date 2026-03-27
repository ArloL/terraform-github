package io.github.arlol.githubcheck.client;

import com.fasterxml.jackson.annotation.JsonInclude;

public record PagesRequest(
		String buildType,
		@JsonInclude(JsonInclude.Include.NON_NULL) Source source,
		@JsonInclude(JsonInclude.Include.NON_NULL) Boolean httpsEnforced
) {

	public record Source(
			String branch,
			String path
	) {
	}

}
