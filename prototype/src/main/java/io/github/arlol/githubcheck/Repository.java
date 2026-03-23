package io.github.arlol.githubcheck;

import java.util.function.Consumer;

public final class Repository {

	private final String name;
	private final RepositoryArgs.Builder builder = RepositoryArgs.builder();

	public Repository(String name) {
		this.name = name;
	}

	public Repository description(String description) {
		builder.description(description);
		return this;
	}

	public Repository homepageUrl(String homepageUrl) {
		builder.homepageUrl(homepageUrl);
		return this;
	}

	public Repository pages() {
		builder.pages();
		return this;
	}

	public Repository archived() {
		builder.archived();
		return this;
	}

	public Repository visibility(String visibility) {
		builder.visibility(visibility);
		return this;
	}

	public Repository requiredStatusChecks(String... checks) {
		builder.requiredStatusChecks(checks);
		return this;
	}

	public Repository actionSecrets(String... secrets) {
		builder.actionSecrets(secrets);
		return this;
	}

	public Repository environment(
			String name,
			Consumer<EnvironmentArgs.Builder> configure
	) {
		builder.environment(name, configure);
		return this;
	}

	public String name() {
		return name;
	}

	public RepositoryArgs args() {
		return builder.build();
	}

}
