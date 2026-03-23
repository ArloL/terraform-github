package io.github.arlol.githubcheck.client;

public record SecurityAndAnalysis(
		StatusObject secretScanning,
		StatusObject secretScanningPushProtection,
		StatusObject advancedSecurity,
		StatusObject dependabotSecurityUpdates
) {
}
