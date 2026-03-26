package io.github.arlol.githubcheck.config;

import io.github.arlol.githubcheck.client.PagesResponse;

public final class PagesArgs {

	public static final PagesResponse.BuildType DEFAULT_BUILD_TYPE = PagesResponse.BuildType.WORKFLOW;
	public static final String DEFAULT_SOURCE_BRANCH = "gh-pages";
	public static final String DEFAULT_SOURCE_PATH = "/";

	private final PagesResponse.BuildType buildType;
	private final String sourceBranch;
	private final String sourcePath;

	private PagesArgs(Builder builder) {
		this.buildType = builder.buildType;
		this.sourceBranch = builder.sourceBranch;
		this.sourcePath = builder.sourcePath;
	}

	public PagesResponse.BuildType buildType() {
		return buildType;
	}

	public String sourceBranch() {
		return sourceBranch;
	}

	public String sourcePath() {
		return sourcePath;
	}

	public static PagesArgs defaults() {
		return builder().build();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private PagesResponse.BuildType buildType = DEFAULT_BUILD_TYPE;
		private String sourceBranch = DEFAULT_SOURCE_BRANCH;
		private String sourcePath = DEFAULT_SOURCE_PATH;

		public Builder buildType(PagesResponse.BuildType buildType) {
			this.buildType = buildType;
			return this;
		}

		public Builder sourceBranch(String sourceBranch) {
			this.sourceBranch = sourceBranch;
			return this;
		}

		public Builder sourcePath(String sourcePath) {
			this.sourcePath = sourcePath;
			return this;
		}

		public PagesArgs build() {
			return new PagesArgs(this);
		}

	}

}
