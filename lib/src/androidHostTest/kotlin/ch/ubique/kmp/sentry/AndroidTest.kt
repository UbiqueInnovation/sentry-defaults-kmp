package ch.ubique.kmp.sentry

import io.sentry.android.core.SentryAndroidOptions
import io.sentry.util.InitUtil
import kotlin.test.Test

class AndroidTest {

	@Suppress("UnstableApiUsage")
	@Test
	fun checkSentryVersions() {
		InitUtil.shouldInit(null, SentryAndroidOptions(), true)
	}

}