package ch.ubique.kmp.sentry

import io.sentry.ILogger
import io.sentry.SentryLevel

class StringLogger() : ILogger {
	private val stringBuilder = StringBuilder()

	override fun log(level: SentryLevel, message: String, vararg args: Any?) {
		log(level, null, message, *args)
	}

	override fun log(level: SentryLevel, message: String, throwable: Throwable?) {
		log(level, throwable, message)
	}

	override fun log(level: SentryLevel, throwable: Throwable?, message: String, vararg args: Any?) {
		stringBuilder.append(level.name).append(": ").append(String.format(message, *args)).append("\n")
		throwable?.let {
			stringBuilder.append(it.toString()).append("\n")
		}
	}

	override fun isEnabled(level: SentryLevel?) = true

	fun getOutput() = stringBuilder.toString()
}