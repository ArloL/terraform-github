package io.github.arlol.githubcheck.config;

import java.util.Arrays;
import java.util.List;

public final class EnvironmentArgs {

	private final String name;
	private final List<String> secrets;

	private EnvironmentArgs(Builder builder) {
		this.name = builder.name;
		this.secrets = List.copyOf(builder.secrets);
	}

	public String name() {
		return this.name;
	}

	public List<String> secrets() {
		return this.secrets;
	}

	public static Builder builder(String name) {
		return new Builder(name);
	}

	public static final class Builder {

		private final String name;
		private List<String> secrets = List.of();

		public Builder(String name) {
			this.name = name;
		}

		public Builder secrets(String... secrets) {
			this.secrets = Arrays.asList(secrets);
			return this;
		}

		public EnvironmentArgs build() {
			return new EnvironmentArgs(this);
		}

	}

}
