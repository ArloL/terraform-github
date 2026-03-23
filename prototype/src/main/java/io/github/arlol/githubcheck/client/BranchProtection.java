package io.github.arlol.githubcheck.client;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public record BranchProtection(
		@JsonDeserialize(
				using = EnabledFieldDeserializer.class
		) boolean enforceAdmins,
		@JsonDeserialize(
				using = EnabledFieldDeserializer.class
		) boolean requiredLinearHistory,
		@JsonDeserialize(
				using = EnabledFieldDeserializer.class
		) boolean allowForcePushes,
		// Absent when no status-check rules are configured.
		RequiredStatusChecks requiredStatusChecks
) {

	public record RequiredStatusChecks(
			boolean strict,
			// Modern API returns checks[].context; legacy returns contexts[].
			List<StatusCheck> checks,
			List<String> contexts
	) {

		public record StatusCheck(
				String context
		) {
		}

	}

	static class EnabledFieldDeserializer extends StdDeserializer<Boolean> {

		EnabledFieldDeserializer() {
			super(Boolean.class);
		}

		@Override
		public Boolean deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException {
			JsonNode node = p.readValueAsTree();
			JsonNode enabled = node.get("enabled");
			if (enabled == null || !enabled.isBoolean()) {
				throw ctxt.instantiationException(
						Boolean.class,
						"expected object with boolean 'enabled' field"
				);
			}
			return enabled.booleanValue();
		}

	}

}
