package ch.ubique.libs.sentry

class NonFatalException(message: String, cause: Throwable? = null) : Exception(message, cause)
