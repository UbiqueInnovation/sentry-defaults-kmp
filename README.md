# Sentry Setup for Kotlin Multiplatform

[![Build](https://github.com/UbiqueInnovation/sentry-defaults-kmp/actions/workflows/build.yml/badge.svg)](https://github.com/UbiqueInnovation/sentry-defaults-kmp/actions/workflows/build.yml)
[![Test](https://github.com/UbiqueInnovation/sentry-defaults-kmp/actions/workflows/test.yml/badge.svg)](https://github.com/UbiqueInnovation/sentry-defaults-kmp/actions/workflows/test.yml)
[![Release](https://github.com/UbiqueInnovation/sentry-defaults-kmp/actions/workflows/release.yml/badge.svg)](https://github.com/UbiqueInnovation/sentry-defaults-kmp/actions/workflows/release.yml)
[![Maven Central](https://img.shields.io/maven-central/v/ch.ubique.kmp/sentry.svg?label=Maven%20Central)](https://search.maven.org/artifact/ch.ubique.kmp/sentry)

## Dependency

Available on Maven Central:
```kotlin
implementation("ch.ubique.kmp:sentry:1.0.0")
```

You may find the current version and version history in the [Releases list](https://github.com/UbiqueInnovation/sentry-defaults-kmp/releases).

## Features

- Auto-installation is off.
- Disables performance tracing and session tracking.
- Anonymizes user and device.
- Disables all breadcrumbs except for the most recently viewed Screen.
- Adds build metadata to crash reports.

## Usage

### 🤖 Android

Call `initUbiqueSentry()` in your Application class' `onCreate()`, or `UbiqueSentry.init()` if elsewhere.

```kotlin
class ExampleApplication : Application() {
    override fun onCreate() {
        initUbiqueSentry(
            isProduction = BuildConfig.IS_FLAVOR_PROD,
            environment = when {
                BuildConfig.DEBUG -> "debug"
                else -> BuildConfig.FLAVOR
            },
            buildTimestamp = BuildConfig.BUILD_TIMESTAMP,
            vcsBranch = BuildConfig.BRANCH,
            alpakaBuildId = BuildConfig.BUILD_ID,
        )
    }
}
```

#### Parameters

| Parameter        | Description                                                                              |
|------------------|------------------------------------------------------------------------------------------|
| `isProduction`   | `true` if this is a production build (public release).                                   |
| `environment`    | Current environment (e.g. `"debug"`, `"dev"`, `"prod"`).                                 |
| `buildTimestamp` | Build timestamp in milliseconds since epoch.                                             |
| `vcsBranch`      | Current VCS branch name (or `null`).                                                     |
| `alpakaBuildId`  | Current Alpaka build ID (or `null`).                                                     |
| `dsn`            | Optional Sentry DSN. If `null`, it will be read from the application's manifest.         |
| `isEnabled`      | Whether Sentry should be immediately enabled (defaults to `true`).                       |
| `beforeSend`     | Callback to modify or drop a `SentryEvent` before it is sent.                            |
| `configuration`  | Callback for further customizations to the `SentryAndroidOptions`.                       |

#### Compose Navigation breadcrumbs

To log Compose Navigation breadcrumbs, 
call [`withSentryObservableEffect()`](https://docs.sentry.io/platforms/android/integrations/jetpack-compose/#configure) 
on your `NavHostController`s:

```kotlin
val navController = rememberNavController().withSentryObservableEffect(
    enableNavigationBreadcrumbs = true,
    enableNavigationTracing = false,
)
```

### 🍎 iOS

Call `initialize()` early in your app's lifecycle (e.g. in your `AppDelegate`):

```swift
import UbiqueSentry

UbiqueSentryKt.initialize(
    isProduction: true,
    environment: "prod",
    buildTimestamp: 1698765432000,
    vcsBranch: "develop",
    dsn: "https://123abc@sentry/42",
)
```

#### Parameters

| Parameter        | Description                                                                   |
|------------------|-------------------------------------------------------------------------------|
| `isProduction`   | `true` if this is a production build (public release).                        | 
| `environment`    | Current environment (e.g. `"debug"`, `"dev"`, `"prod"`).                      |
| `buildTimestamp` | Build timestamp in milliseconds since epoch.                                  |
| `vcsBranch`      | Current VCS branch name (or `nil`).                                           |
| `dsn`            | Optional Sentry DSN. If `nil`, the SDK reads it from SENTRY_DSN env variable. |
| `isEnabled`      | Whether Sentry should be immediately enabled (defaults to `true`).            |
| `beforeSend`     | Callback to modify or drop a `SentryEvent` before it is sent.                 |
| `configuration`  | Callback for further customizations to the Sentry options.                    |

### 🌟 Multiplatform

#### Enable at runtime

To enable or disable Sentry at runtime, set `UbiqueSentry.isEnabled` at your will.

#### Non-fatal exceptions

Use `UbiqueSentry.captureException()` to record non-fatal exceptions.

#### Messages

Use `UbiqueSentry.captureMessage()` to send a message as a non-fatal exception to Sentry:

The message is wrapped in a `NonFatalException` internally and supports the same optional `level`, `extras`, 
and `scopeCallback` parameters as `captureException()`.

#### Breadcrumbs

Use `UbiqueSentry.addBreadcrumb()` to add a breadcrumb to the current Sentry scope.

The `breadcrumb` parameter accepts a `Breadcrumb` instance (e.g. created via `Breadcrumb.ui()`, `Breadcrumb.navigation()`, etc.), 
and the optional `data` parameter allows attaching additional key-value pairs to the breadcrumb.

#### Test Crash

Use `UbiqueSentry.crash()` to trigger a test crash by throwing an Exception.

---

## Development & Testing

Most features of this library can be implemented with test-driven development using unit tests with a mock webserver instance.

To test any changes locally in an app, you can either include the library via dependency substitution in an application project,
or deploy a build to your local maven repository and include that from any application:

1. Define a unique custom version by setting the `VERSION_NAME` variable in the `gradle.properties` file.
2. Deploy the library artifact by running `./gradlew publishToMavenLocal`
3. Reference the local maven repository in your application's build script:

    ```kotlin
    repositories {
        mavenLocal()
    }
    ```

4. And apply the local library version:

    ```kotlin
    dependencies {
        implementation("ch.ubique.kmp:sentry:$yourLocalVersion")
    }
    ```

---

## Deployment

Create a [Release](https://github.com/UbiqueInnovation/sentry-defaults-kmp/releases),
setting the Tag to the desired version prefixed with a `v`.

Each release on GitHub will be deployed to Maven Central.

* Group: `ch.ubique.kmp`
* Artifact: `sentry`
* Version: `major.minor.revision`
