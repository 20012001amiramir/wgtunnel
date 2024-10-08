plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.hilt.android)
	alias(libs.plugins.kotlinxSerialization)
	alias(libs.plugins.ksp)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.grgit)
}

val versionFile = file("$rootDir/versionCode.txt")

val versionCodeIncrement = with(getBuildTaskName().lowercase()) {
	when {
		this.contains(Constants.NIGHTLY) || this.contains(Constants.PRERELEASE) -> {
			if (versionFile.exists()) {
				versionFile.readText().toInt() + 1
			} else {
				1
			}
		}
		else -> 0
	}
}

android {
	namespace = Constants.APP_ID
	compileSdk = Constants.TARGET_SDK

	androidResources {
		generateLocaleConfig = true
	}

	defaultConfig {
		applicationId = Constants.APP_ID
		minSdk = Constants.MIN_SDK
		targetSdk = Constants.TARGET_SDK
		versionCode = Constants.VERSION_CODE + versionCodeIncrement
		versionName = determineVersionName()

		ksp { arg("room.schemaLocation", "$projectDir/schemas") }

		sourceSets {
			getByName("debug").assets.srcDirs(files("$projectDir/schemas")) // Room
		}

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		vectorDrawables { useSupportLibrary = true }
	}

	signingConfigs {
		create(Constants.RELEASE) {
			storeFile = getStoreFile()
			storePassword = getSigningProperty(Constants.STORE_PASS_VAR)
			keyAlias = getSigningProperty(Constants.KEY_ALIAS_VAR)
			keyPassword = getSigningProperty(Constants.KEY_PASS_VAR)
		}
	}

	buildTypes {
		// don't strip
		packaging.jniLibs.keepDebugSymbols.addAll(
			listOf("libwg-go.so", "libwg-quick.so", "libwg.so"),
		)

		release {
			isDebuggable = false
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro",
			)
			signingConfig = signingConfigs.getByName(Constants.RELEASE)
		}
		debug {
			applicationIdSuffix = ".debug"
			versionNameSuffix = "-debug"
			resValue("string", "app_name", "CatVpn - Debug")
			isDebuggable = true
		}

		create(Constants.PRERELEASE) {
			initWith(buildTypes.getByName(Constants.RELEASE))
			applicationIdSuffix = ".prerelease"
			versionNameSuffix = "-pre"
			resValue("string", "app_name", "CatVpn - Pre")
		}

		create(Constants.NIGHTLY) {
			initWith(buildTypes.getByName(Constants.RELEASE))
			applicationIdSuffix = ".nightly"
			versionNameSuffix = "-nightly"
			resValue("string", "app_name", "CatVpn - Nightly")
		}

		applicationVariants.all {
			val variant = this
			variant.outputs
				.map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
				.forEach { output ->
					val outputFileName =
						"${Constants.APP_NAME}-${variant.flavorName}-" +
							"${variant.buildType.name}-${variant.versionName}.apk"
					output.outputFileName = outputFileName
				}
		}
	}
	flavorDimensions.add(Constants.TYPE)
	productFlavors {
		create("fdroid") {
			dimension = Constants.TYPE
			proguardFile("fdroid-rules.pro")
		}
		create("general") {
			dimension = Constants.TYPE
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
		isCoreLibraryDesugaringEnabled = true
	}
	kotlinOptions { jvmTarget = Constants.JVM_TARGET }
	buildFeatures {
		compose = true
		buildConfig = true
	}
	packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

val generalImplementation by configurations

dependencies {

	implementation(project(":logcatter"))

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)

	// helpers for implementing LifecycleOwner in a Service
	implementation(libs.androidx.lifecycle.service)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.material3)
	implementation(libs.androidx.appcompat)
	implementation (libs.google.accompanist.systemuicontroller)
	implementation(libs.haze)
	implementation(libs.haze.materials)
	implementation(libs.coil.compose)
	//retrofit
	implementation(libs.retrofit2)
	implementation(libs.retrofit2.converter)
	implementation(libs.stetho.facebook)
	implementation(libs.stetho.urlconnection)
	implementation(libs.stetho.okhttp3)
	implementation (libs.androidx.constraintlayout.compose)
	implementation (libs.touchlane.gridpad)

	debugImplementation(libs.androidx.ui.tooling.v170)
	implementation(libs.androidx.ui.tooling.preview.v170)
	// test
	testImplementation(libs.junit)
	testImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test)
	androidTestImplementation(libs.androidx.room.testing)
	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.manifest)
	implementation (libs.kotlinx.coroutines.play.services)
	implementation(libs.maplibre.compose)
	// get tunnel lib from github packages or mavenLocal
// 	implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
	implementation(libs.tunnel)
	implementation(libs.amneziawg.android)
	coreLibraryDesugaring(libs.desugar.jdk.libs)

	// logging
	implementation(libs.timber)

	// compose navigation
	implementation(libs.androidx.navigation.compose)
	implementation(libs.androidx.hilt.navigation.compose)

	// hilt
	implementation(libs.hilt.android)
	ksp(libs.hilt.android.compiler)

	// accompanist
	implementation(libs.accompanist.permissions)
	implementation(libs.accompanist.flowlayout)
	implementation(libs.accompanist.drawablepainter)

	// storage
	implementation(libs.androidx.room.runtime)
	ksp(libs.androidx.room.compiler)
	implementation(libs.androidx.room.ktx)
	implementation(libs.androidx.datastore.preferences)

	// lifecycle
	implementation(libs.lifecycle.runtime.compose)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.lifecycle.process)

	// icons
	implementation(libs.material.icons.extended)
	// serialization
	implementation(libs.kotlinx.serialization.json)

	// barcode scanning
	implementation(libs.zxing.android.embedded)

	// bio
	implementation(libs.androidx.biometric.ktx)
	implementation(libs.pin.lock.compose)

	// shortcuts
	implementation(libs.androidx.core)
	implementation(libs.androidx.core.google.shortcuts)

	// splash
	implementation(libs.androidx.core.splashscreen)
}

fun determineVersionName(): String {
	return with(getBuildTaskName().lowercase()) {
		when {
			contains(Constants.NIGHTLY) || contains(Constants.PRERELEASE) ->
				Constants.VERSION_NAME +
					"-${grgitService.service.get().grgit.head().abbreviatedId}"
			else -> Constants.VERSION_NAME
		}
	}
}

val incrementVersionCode by tasks.registering {
	doLast {
		val versionFile = file("$rootDir/versionCode.txt")
		if (versionFile.exists()) {
			versionFile.writeText(versionCodeIncrement.toString())
			println("Incremented versionCode to $versionCodeIncrement")
		}
	}
}

tasks.whenTaskAdded {
	if (name.startsWith("assemble") && !name.lowercase().contains("debug")) {
		dependsOn(incrementVersionCode)
	}
}
