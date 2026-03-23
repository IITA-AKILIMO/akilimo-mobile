import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.LocalDateTime

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.sentry.gradle)
    alias(libs.plugins.google.services)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.detekt)
    // alias(libs.plugins.firebase.crashlytics)
}
fun q(v: String?) = "\"${v?.trim()?.removeSurrounding("\"")}\""

tasks.named("sonar") {
    dependsOn("lint", "detekt")
}


detekt {
    baseline = file("detekt-baseline.xml")
}


sonar {
    properties {
        property(
            "sonar.projectKey",
            env.SONAR_PROJECT_KEY.orElse("IITA-AKILIMO_akilimo-mobile_106faa56-1bba-4864-9abf-11fec67c8c31")
        )
        property("sonar.host.url", env.SONAR_HOST_URL.value)
        property("sonar.token", env.SONAR_TOKEN.value)

        property("sonar.sources", "src")
        property("sonar.sourceEncoding", "UTF-8")

        property("sonar.test.inclusions", "**/*Test*/**")
        property(
            "sonar.exclusions",
            listOf(
                " **/generated/**",
                " **/build/**",
                " **/debug/**",
                "**/.gradle/**",
                "**/androidTest/**",
                "**/R.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/android/databinding/*",
                "**/androidx/databinding/*",
                "**/*MapperImpl*.*",
                "**/google-services.json"
            ).joinToString(",")
        )

        property(
            "sonar.androidLint.reportPaths",
            layout.buildDirectory.file("reports/custom-lint-results.xml").get().asFile.path
        )
        property(
            "sonar.kotlin.detekt.reportPaths",
            layout.buildDirectory.file("reports/detekt/detekt.xml").get().asFile.path
        )
    }
}
android {
    val releaseVersionName = computeBuildNumber()
    val appVersionCode = computeVersionCode()

    namespace = "com.akilimo.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.akilimo.mobile"
        minSdk = 21
        targetSdk = 36
        versionCode = appVersionCode
        versionName = releaseVersionName

        testInstrumentationRunner = "com.akilimo.mobile.HiltTestRunner"

        val fuelrodBaseUrl =
            env.FUELROD_BASE_URL.orElse("https://akilimo.fuelrod.com")
        val akilimoBaseUrl = env.AKILIMO_BASE_URL.orElse("https://api.akilimo.org")
        val mapboxRuntimeToken = env.MAPBOX_RUNTIME_TOKEN.orElse("")
        val locationIqToken = env.LOCATION_IQ_TOKEN.orElse("")

        buildConfigField("String", "AKILIMO_BASE_URL", q(akilimoBaseUrl))
        buildConfigField("String", "FUELROD_BASE_URL", q(fuelrodBaseUrl))
        buildConfigField("String", "MAPBOX_RUNTIME_TOKEN", q(mapboxRuntimeToken))
        buildConfigField("String", "LOCATION_IQ_TOKEN", q(locationIqToken))
    }

    lint {
        xmlReport = true
        htmlReport = false
        ignoreWarnings = true
        abortOnError = false
        checkReleaseBuilds = false
        baseline = file("lint-baseline.xml")
        xmlOutput = layout.buildDirectory.file("/reports/custom-lint-results.xml").get().asFile
    }

    bundle {
        language {
            // Disable per-language APK splits so all locale resources (sw-rTZ, rw-rRW, etc.)
            // are included in every install. Without this, Play Store strips languages that
            // don't match the device locale, breaking in-app language switching.
            enableSplit = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isDebuggable = true
            resValue("string", "PORT_NUMBER", "9085")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding = false
        buildConfig = true
        compose = true
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
//    kotlinOptions {
//        jvmTarget = "17"
//    }
}


fun computeVersionName(): String {
    val now = LocalDateTime.now()

    val defaultMajor = 30
    var defaultMinor = now.monthValue
    var defaultBuild = now.dayOfMonth

    val branch = System.getenv("BRANCH_NAME") ?: "dev"
    if (branch == "dev") {
        defaultMinor = 0
        defaultBuild = 0
    }

    val major = System.getenv("VERSION_MAJOR")?.toIntOrNull() ?: defaultMajor
    val minor = System.getenv("VERSION_MINOR")?.toIntOrNull() ?: defaultMinor
    val build = System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: defaultBuild

    return "%d.%d.%d".format(major, minor, build)
}

fun computeBuildNumber(): String {
    val file = File("nextrelease.txt").apply { createNewFile() }

    val tag = System.getenv("RELEASE_VERSION") ?: computeVersionName()

    println("Current build tag is: $tag")

    file.writeText(tag)
    return tag
}

fun computeVersionCode(): Int {
    val code = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
    println("Version code is $code")
    return code
}

dependencies {

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // Region: Compose
    implementation(platform(libs.androidx.compose.bom))
    // Region: AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.work)

    // Region: Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    //Region: Google services
    implementation(libs.play.services.location)
    //Region: Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.installations)
    implementation(libs.firebase.analytics)

    // Region: Non-Compose UI
    implementation(libs.material)

    // Region: Navigation
    implementation(libs.bundles.androidx.navigation)

    // Region: DataStore
    implementation(libs.datastore.preferences)

    // Region: Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Region: Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Region: Networking
    implementation(libs.okhttp)
    implementation(libs.okhttp.inteceptor)
    implementation(libs.retrofit)
    implementation(libs.moshi.converter)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    // Region: Mapbox
    implementation(libs.mapbox.sdk)
//    implementation(libs.mapbox.annotation)
//    implementation(libs.mapbox.places)

    // Region: Utility Libraries
    implementation(libs.applocale)
    implementation(libs.reword)
    implementation(libs.viewpump)
    implementation(libs.worldcountrydata)
    implementation(libs.jakewharton.timber)

    implementation(libs.country.code.picker)

    implementation(libs.viewpager2)

    // Region: Testing
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.hilt.testing)
    androidTestImplementation(libs.navigation.testing)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.coroutines.test)
    kaptAndroidTest(libs.hilt.compiler)

    // Region: Debug Tools
    debugImplementation(libs.debug.db)
    debugImplementation(libs.stetho)
    debugImplementation(libs.stetho.okhttp)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}