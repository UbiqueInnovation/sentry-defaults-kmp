package ch.ubique.libs.sentry

import cocoapods.Sentry.SentryEvent
import cocoapods.Sentry.SentryHttpStatusCodeRange
import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryPlatformOptions
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNumber
import kotlin.time.Instant

/**
 * @param isProduction true if this is a production build, i.e. public release.
 * @param environment current environment (e.g. "debug", "dev", "prod").
 * @param buildTimestamp build timestamp in milliseconds since epoch.
 * @param vcsBranch current VCS branch name.
 * @param dsn optional Sentry DSN, if not provided, it will be read from the application's manifest.
 */
fun initialize(
	isProduction: Boolean,
	environment: String,
	buildTimestamp: Long,
	vcsBranch: String?,
	dsn: String? = null,
) {
	initialize(
		isProduction = isProduction,
		environment = environment,
		buildTimestamp = buildTimestamp,
		vcsBranch = vcsBranch,
		dsn = dsn,
	)
}

/**
 * @param isProduction true if this is a production build, i.e. public release.
 * @param environment current environment (e.g. "debug", "dev", "prod").
 * @param buildTimestamp build timestamp in milliseconds since epoch.
 * @param vcsBranch current VCS branch name.
 * @param dsn optional Sentry DSN, if not provided, it will be read from the application's manifest.
 * @param isEnabled if Sentry should be immediately enabled, defaults to true.
 * @param beforeSend callback to modify a SentryEvent.
 * @param configuration callback for further customizations to the SentryAndroidOptions.
 */
@OptIn(ExperimentalForeignApi::class)
fun initialize(
	isProduction: Boolean,
	environment: String,
	buildTimestamp: Long,
	vcsBranch: String?,
	dsn: String? = null,
	isEnabled: Boolean = UbiqueSentry.isEnabled,
	beforeSend: (event: SentryEvent) -> SentryEvent? = { event -> event },
	configuration: (SentryPlatformOptions) -> Unit = {},
) {
	UbiqueSentry.isEnabled = isEnabled

	Sentry.initWithPlatformOptions { options ->
		dsn?.let { options.dsn = it }

		options.environment = environment

		options.enableAutoSessionTracking = false

		// opt-out from attaching a stacktrace to non-error events
		options.attachStacktrace = false

		options.beforeSend = beforeSend@{ event ->
			if (event == null || !isEnabled) {
				return@beforeSend null
			}

			// remove user ID and device app hash from all events
			event.user?.userId = null
			(event.context?.get("app") as MutableMap<*, *>?)?.apply {
				remove("device_app_hash")
			}
			(event.context?.get("device") as MutableMap<*, *>?)?.apply {
				remove("id")
				remove("boot_time")
			}

			// add build information
			event.setTags(
				buildMap {
					buildTimestamp.takeIf { it != 0L }?.let {
						put("build.datetime", Instant.fromEpochMilliseconds(it).toString())
						put("build.timestamp", it.toString())
					}
					vcsBranch?.let {
						put("vcs.branch", it)
					}
				}
			)

			beforeSend(event)
		}

		if (isProduction) {
			options.enableAutoBreadcrumbTracking = false
			options.maxBreadcrumbs = 1UL
		}

		// disable performance tracing on prod
		options.tracesSampleRate = if (!isProduction) NSNumber(1.0) else null

		if (isProduction) {
			options.enableTracing = false
			options.enableNetworkBreadcrumbs = false
			options.enableCaptureFailedRequests = false
		} else {
			options.enableTracing = true
			options.enableNetworkBreadcrumbs = false
			options.enableCaptureFailedRequests = false  // Track 4xx - 5xx network errors
			options.failedRequestStatusCodes = listOf(SentryHttpStatusCodeRange(min = 400, max = 599))
		}

		configuration(options)
	}
}
