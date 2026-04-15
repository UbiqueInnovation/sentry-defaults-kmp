package ch.ubique.kmp.sentry

import io.sentry.SentryIntegrationPackageStorage
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.util.InitUtil
import kotlin.test.Test
import kotlin.test.assertFalse

class AndroidTest {

	@Suppress("UnstableApiUsage")
	@Test
	fun checkSentryVersions() {
		// direct test
		val logger = StringLogger()
		val sentryOptions = SentryAndroidOptions()
		sentryOptions.setFatalLogger(logger)
		assertFalse(
			SentryIntegrationPackageStorage.getInstance().checkForMixedVersions(logger),
			logger.getOutput(),
		)

		// indirect test (throws if versions are mixed)
		InitUtil.shouldInit(null, SentryAndroidOptions(), true)
	}

}