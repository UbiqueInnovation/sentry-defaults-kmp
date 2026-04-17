package ch.ubique.libs.sentry

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import io.sentry.Hint
import io.sentry.SentryEvent
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.android.fragment.FragmentLifecycleIntegration
import io.sentry.android.fragment.FragmentLifecycleState
import io.sentry.protocol.App
import io.sentry.protocol.Device
import kotlin.time.Instant

/**
 * @param context
 * @param isProduction true if this is a production build, i.e. public release.
 * @param environment current environment (e.g. "dev", "int", "prod").
 * @param buildTimestamp build timestamp in milliseconds since epoch.
 * @param buildNumber current build number.
 * @param vcsBranch current VCS branch name.
 * @param alpakaBuildId current Alpaka build ID.
 * @param dsn optional Sentry DSN, if not provided, it will be read from the application's manifest.
 * @param isEnabled if Sentry should be immediately enabled, defaults to true.
 * @param beforeSend callback to modify a SentryEvent.
 * @param configuration callback for further customizations to the SentryAndroidOptions.
 */
fun UbiqueSentry.init(
	context: Context,
	isProduction: Boolean,
	environment: String,
	buildTimestamp: Long,
	buildNumber: Long?,
	vcsBranch: String,
	alpakaBuildId: String,
	dsn: String? = null,
	isEnabled: Boolean = UbiqueSentry.isEnabled,
	beforeSend: (event: SentryEvent, hint: Hint) -> SentryEvent? = { event, _ -> event },
	configuration: (SentryAndroidOptions) -> Unit = {},
) {
	val application = context.applicationContext as Application
	application.initUbiqueSentry(
		isProduction = isProduction,
		environment = environment,
		buildTimestamp = buildTimestamp,
		buildNumber = buildNumber,
		vcsBranch = vcsBranch,
		alpakaBuildId = alpakaBuildId,
		dsn = dsn,
		isEnabled = isEnabled,
		beforeSend = beforeSend,
		configuration = configuration,
	)
}

/**
 * @param isProduction true if this is a production build, i.e. public release.
 * @param environment current environment (e.g. "debug", "dev", "prod").
 * @param buildTimestamp build timestamp in milliseconds since epoch.
 * @param buildNumber current build number.
 * @param vcsBranch current VCS branch name.
 * @param alpakaBuildId current Alpaka build ID.
 * @param dsn optional Sentry DSN, if not provided, it will be read from the application's manifest.
 * @param isEnabled if Sentry should be immediately enabled, defaults to true.
 * @param beforeSend callback to modify a SentryEvent.
 * @param configuration callback for further customizations to the SentryAndroidOptions.
 */
fun Application.initUbiqueSentry(
	isProduction: Boolean,
	environment: String,
	buildTimestamp: Long?,
	buildNumber: Long?,
	vcsBranch: String?,
	alpakaBuildId: String?,
	dsn: String? = null,
	isEnabled: Boolean = UbiqueSentry.isEnabled,
	beforeSend: (event: SentryEvent, hint: Hint) -> SentryEvent? = { event, _ -> event },
	configuration: (SentryAndroidOptions) -> Unit = {},
) {
	UbiqueSentry.isEnabled = isEnabled

	SentryAndroid.init(this) { options ->
		val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

		dsn?.let { options.dsn = it }

		options.environment = environment

		options.isEnableAutoSessionTracking = false

		// opt-out from attaching a stacktrace to non-error events
		options.isAttachStacktrace = false

		// disable performance tracing on prod
		options.tracesSampleRate = if (!isProduction) 1.0 else null

		options.setBeforeSend { event, hint ->
			if (!isEnabled) {
				return@setBeforeSend null
			}

			// remove user ID and device app hash from all events
			event.user?.id = null
			(event.contexts["app"] as App?)?.apply {
				deviceAppHash = null
			}
			(event.contexts["device"] as Device?)?.apply {
				id = null
				bootTime = null
			}

			// add build information
			buildTimestamp?.let {
				event.setTag("build.datetime", Instant.fromEpochMilliseconds(it).toString())
				event.setTag("build.timestamp", it.toString())
			}
			buildNumber?.let {
				event.setTag("build.number", it.toString())
			}
			alpakaBuildId?.let {
				event.setTag("alpaka.build_id", it)
			}
			vcsBranch?.let {
				event.setTag("vcs.branch", it)
			}

			beforeSend(event, hint)
		}

		if (isProduction && !isDebug) {
			options.apply {
				enableAllAutoBreadcrumbs(false)
				isEnableActivityLifecycleBreadcrumbs = true
				maxBreadcrumbs = 1
			}
		}

		options.addIntegration(
			FragmentLifecycleIntegration(
				this,
				filterFragmentLifecycleBreadcrumbs = setOf(
					FragmentLifecycleState.CREATED,
					FragmentLifecycleState.STARTED,
					FragmentLifecycleState.RESUMED,
					FragmentLifecycleState.PAUSED,
					FragmentLifecycleState.STOPPED,
				),
				enableAutoFragmentLifecycleTracing = true,
			)
		)

		configuration(options)
	}
}
