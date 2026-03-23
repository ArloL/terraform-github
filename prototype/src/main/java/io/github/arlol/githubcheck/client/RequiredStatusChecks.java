package io.github.arlol.githubcheck.client;

import java.util.List;

public record RequiredStatusChecks(
		boolean strict,
		// Modern API returns checks[].context; legacy returns contexts[].
		List<StatusCheck> checks,
		List<String> contexts
) {
}
