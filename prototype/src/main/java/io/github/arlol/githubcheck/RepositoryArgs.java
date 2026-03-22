package io.github.arlol.githubcheck;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class RepositoryArgs {

	private final boolean archived;
	private final boolean githubPages;
	private final String description;
	private final String homepageUrl;
	private final String visibility;
	private final List<String> requiredStatusChecks;
	private final List<String> actionSecrets;
	private final Map<String, EnvironmentArgs> environments;

	private RepositoryArgs(Builder builder) {
		this.archived = builder.archived;
		this.githubPages = builder.githubPages;
		this.description = builder.description;
		this.homepageUrl = builder.homepageUrl;
		this.visibility = builder.visibility;
		this.requiredStatusChecks = List.copyOf(builder.requiredStatusChecks);
		this.actionSecrets = List.copyOf(builder.actionSecrets);
		this.environments = Collections
				.unmodifiableMap(new LinkedHashMap<>(builder.environments));
	}

	public boolean archived() {
		return archived;
	}

	public boolean githubPages() {
		return githubPages;
	}

	public String description() {
		return description;
	}

	public String homepageUrl() {
		return homepageUrl;
	}

	public String visibility() {
		return visibility;
	}

	/** Extra status checks beyond the four applied to all public repos. */
	public List<String> requiredStatusChecks() {
		return requiredStatusChecks;
	}

	public List<String> actionSecrets() {
		return actionSecrets;
	}

	public Map<String, EnvironmentArgs> environments() {
		return environments;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private boolean archived = false;
		private boolean githubPages = false;
		private String description = "";
		private String homepageUrl = "";
		private String visibility = "public";
		private List<String> requiredStatusChecks = List.of();
		private List<String> actionSecrets = List.of();
		private final Map<String, EnvironmentArgs> environments = new LinkedHashMap<>();

		public Builder archived() {
			this.archived = true;
			return this;
		}

		public Builder githubPages() {
			this.githubPages = true;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder homepageUrl(String homepageUrl) {
			this.homepageUrl = homepageUrl;
			return this;
		}

		public Builder visibility(String visibility) {
			this.visibility = visibility;
			return this;
		}

		public Builder requiredStatusChecks(String... checks) {
			this.requiredStatusChecks = Arrays.asList(checks);
			return this;
		}

		public Builder actionSecrets(String... secrets) {
			this.actionSecrets = Arrays.asList(secrets);
			return this;
		}

		public Builder environment(String name, EnvironmentArgs args) {
			this.environments.put(name, args);
			return this;
		}

		public Builder environment(
				String name,
				Consumer<EnvironmentArgs.Builder> configure
		) {
			var envBuilder = EnvironmentArgs.builder();
			configure.accept(envBuilder);
			this.environments.put(name, envBuilder.build());
			return this;
		}

		public RepositoryArgs build() {
			return new RepositoryArgs(this);
		}

	}

}
