import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
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
    namespace = "com.akilimo.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.akilimo.mobile"
        minSdk = 21
        targetSdk = 36
        versionCode = 30
        versionName = "30.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val fuelrodBaseUrl =
            env.FUELROD_BASE_URL.orElse("https://akilimo.fuelrod.com")
        val akilimoBaseUrl = env.AKILIMO_BASE_URL.orElse("https://api.akilimo.org")

        buildConfigField("String", "AKILIMO_BASE_URL", q(akilimoBaseUrl))
        buildConfigField("String", "FUELROD_BASE_URL", q(fuelrodBaseUrl))
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
        viewBinding = true
        buildConfig = true
        compose = false
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

dependencies {

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    // Region: AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.work)

    //Region: Firebase
    implementation(libs.firebase.bom)
    implementation(libs.firebase.installations)

    // Region: Non-Compose UI
    implementation(libs.material)

    // Region: Navigation
    implementation(libs.bundles.androidx.navigation)

    // Region: Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Region: Networking
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.moshi.converter)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    // Region: Mapbox
    implementation(libs.mapbox.sdk)
    implementation(libs.mapbox.annotation)
    implementation(libs.mapbox.places)

    // Region: Utility Libraries
    implementation(libs.applocale)
    implementation(libs.reword)
    implementation(libs.viewpump)
    implementation(libs.worldcountrydata)
    implementation(libs.process.phoenix)
    implementation(libs.jakewharton.timber)

    implementation(libs.country.code.picker)

    // Region: Stepper UI
    implementation(libs.material.stepper)

    // Region: Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Region: Debug Tools
    debugImplementation(libs.debug.db)
    debugImplementation(libs.stetho)
    debugImplementation(libs.stetho.okhttp)
}