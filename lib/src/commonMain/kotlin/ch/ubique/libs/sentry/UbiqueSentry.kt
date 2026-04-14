package ch.ubique.libs.sentry

import io.sentry.kotlin.multiplatform.Scope
import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryLevel
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object UbiqueSentry {

	var isEnabled = true

	// Synchronizes capturing of sentryErrors and adding breadcrumbs via a serialized queue.
	// This was needed so that on iOS simulators / debug builds, capturing errors would not delay
	// the displaying of the error in the UI, but rather be a fire-and-forget action (probably a bug by sentry, kmp config, ...)
	private val sentryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))

	/**
	 * Captures a non-fatal exception with Sentry.
	 * @param throwable the exception to capture.
	 * @param level the severity level of the event, defaults to INFO.
	 * @param extras a map of additional key-value pairs to include as extras in the event, defaults to an empty map.
	 * @param scopeCallback a callback function that receives the Sentry Scope, allowing for additional configuration of the event before it is sent.
	 */
	fun captureException(
		throwable: Throwable,
		level: SentryLevel = SentryLevel.INFO,
		extras: Map<String, String> = emptyMap(),
		scopeCallback: (scope: Scope) -> Unit = {},
	) {
		if (isEnabled) {
			sentryScope.launch {
				Sentry.captureException(throwable) { scope ->
					scope.level = level
					extras.forEach { (key, value) ->
						scope.setExtra(key, value)
					}
					scopeCallback(scope)
				}
			}
		}
	}

	/**
	 * Captures a message as a non-fatal exception with Sentry.
	 * @param message the message to capture.
	 * @param level the severity level of the event, defaults to INFO.
	 * @param extras a map of additional key-value pairs to include as extras in the event, defaults to an empty map.
	 * @param scopeCallback a callback function that receives the Sentry Scope, allowing for additional configuration of the event before it is sent.
	 */
	fun captureMessage(
		message: String,
		level: SentryLevel = SentryLevel.INFO,
		extras: Map<String, String> = emptyMap(),
		scopeCallback: (scope: Scope) -> Unit = {},
	) {
		captureException(NonFatalException(message), level, extras, scopeCallback)
	}

	/**
	 * Adds a breadcrumb to the current Sentry scope.
	 * @param breadcrumb the breadcrumb to add, created with the desired `Breadcrumb.xxx()` method.
	 * @param data an optional map of additional key-value pairs to include as data in the breadcrumb.
	 */
	fun addBreadcrumb(breadcrumb: Breadcrumb, data: MutableMap<String, Any>? = null) {
		if (isEnabled) {
			sentryScope.launch {
				data?.forEach { (key, value) ->
					breadcrumb.setData(key, value)
				}
				Sentry.addBreadcrumb(breadcrumb)
			}
		}
	}

	/**
	 * Enforces a crash for testing purposes by throwing an Exception.
	 */
	fun crash() {
		throw TaskFailedSuccessfullyException()
	}

}
