@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.android.library)
	alias(libs.plugins.vanniktech.publish)
}

kotlin {
	jvmToolchain(17)

	android {
		namespace = "ch.ubique.libs.sentry"
		compileSdk = 36
		minSdk = 23

		optimization {
			consumerKeepRules.apply {
				publish = true
				file("consumer-rules.pro")
			}
		}
	}

	val xcf = XCFramework()
	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64(),
	).forEach {
		it.binaries.framework {
			baseName = "UbiqueSentry"
			isStatic = true
			xcf.add(this)
		}
	}

	applyDefaultHierarchyTemplate()

	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlinx.coroutines.core)
			api(libs.sentry.kmp)
		}
		androidMain.dependencies {
			api(libs.sentry.android.fragment)
			api(libs.sentry.android.navigation)
		}
		iosMain.dependencies {
		}
	}

	compilerOptions {
		freeCompilerArgs.add("-Xexpect-actual-classes")
	}
}

mavenPublishing {
	coordinates(version = project.version.toString())
	publishToMavenCentral(true)
	signAllPublications()
}
