package io.github.arlol.githubcheck;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class RepositoryArgs {

	private final String name;
	private final boolean archived;
	private final boolean pages;
	private final String description;
	private final String homepageUrl;
	private final String visibility;
	private final List<String> requiredStatusChecks;
	private final List<String> actionsSecrets;
	private final Map<String, EnvironmentArgs> environments;

	private RepositoryArgs(Builder builder) {
		this.name = builder.name;
		this.archived = builder.archived;
		this.pages = builder.pages;
		this.description = builder.description;
		this.homepageUrl = builder.homepageUrl;
		this.visibility = builder.visibility;
		this.requiredStatusChecks = List.copyOf(builder.requiredStatusChecks);
		this.actionsSecrets = List.copyOf(builder.actionsSecrets);
		this.environments = Collections
				.unmodifiableMap(new LinkedHashMap<>(builder.environments));
	}

	public String name() {
		return name;
	}

	public boolean archived() {
		return archived;
	}

	public boolean pages() {
		return pages;
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

	/**
	 * Extra status checks beyond the four applied to all public repos.
	 */
	public List<String> requiredStatusChecks() {
		return requiredStatusChecks;
	}

	public List<String> actionsSecrets() {
		return actionsSecrets;
	}

	public Map<String, EnvironmentArgs> environments() {
		return environments;
	}

	public Builder toBuilder() {
		return new Builder(this);
	}

	public static Builder create(String name) {
		return new Builder(name);
	}

	public static RepositoryArgs archived(String name) {
		return new Builder(name).archived().build();
	}

	public static final class Builder {

		private final String name;
		private boolean archived = false;
		private boolean pages = false;
		private String description = "";
		private String homepageUrl = "";
		private String visibility = "public";
		private List<String> requiredStatusChecks = List.of();
		private List<String> actionsSecrets = List.of();
		private final Map<String, EnvironmentArgs> environments = new LinkedHashMap<>();

		public Builder(String name) {
			this.name = name;
		}

		public Builder(RepositoryArgs repositoryArgs) {
			this.name = repositoryArgs.name;
			this.archived = repositoryArgs.archived;
			this.pages = repositoryArgs.pages;
			this.description = repositoryArgs.description;
			this.homepageUrl = repositoryArgs.homepageUrl;
			this.visibility = repositoryArgs.visibility;
			this.requiredStatusChecks = repositoryArgs.requiredStatusChecks;
			this.actionsSecrets = repositoryArgs.actionsSecrets;
			this.environments.putAll(repositoryArgs.environments);
		}

		public Builder archived() {
			this.archived = true;
			return this;
		}

		public Builder pages() {
			this.pages = true;
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

		public Builder actionsSecrets(String... secrets) {
			this.actionsSecrets = Arrays.asList(secrets);
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
			var envBuilder = EnvironmentArgs.builder(name);
			configure.accept(envBuilder);
			this.environments.put(name, envBuilder.build());
			return this;
		}

		public RepositoryArgs build() {
			return new RepositoryArgs(this);
		}

	}

}
