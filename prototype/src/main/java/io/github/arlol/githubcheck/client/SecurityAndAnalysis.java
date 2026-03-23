package io.github.arlol.githubcheck.client;

public record SecurityAndAnalysis(
		StatusObject secretScanning,
		StatusObject secretScanningPushProtection,
		StatusObject advancedSecurity,
		StatusObject dependabotSecurityUpdates,
		StatusObject codeSecurity,
		StatusObject secretScanningNonProviderPatterns,
		StatusObject secretScanningAiDetection,
		StatusObject secretScanningDelegatedAlertDismissal,
		StatusObject secretScanningDelegatedBypass
) {

	public record StatusObject(
			String status
	) {
	}

}
