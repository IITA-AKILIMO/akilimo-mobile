# AKILIMO R8 rules.
# Libraries (Retrofit, OkHttp, Room, Hilt, Mapbox, Sentry, WorkManager, Compose) ship their
# own consumer rules; these cover reflection done by THIS app's code.

# Readable stack traces in Sentry (mapping is uploaded by the Sentry Gradle plugin when
# SENTRY_AUTH_TOKEN is set).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Generic signatures and annotations are needed by Retrofit and Moshi.
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# --- Moshi -------------------------------------------------------------------------------
# ApiClient uses KotlinJsonAdapterFactory (reflection), which reads Kotlin metadata and
# constructor/property names of every JSON model. Keep the metadata and the models.
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { public <methods>; }

# Request/response models and DTOs sent to or parsed from the AKILIMO, weather and geocoding APIs.
-keep class com.akilimo.mobile.network.** { *; }
-keep class com.akilimo.mobile.dto.** { *; }

# Entities are also used in API payloads and via toEntity() mappers; enums are serialized
# by name (Moshi reads the enum fields reflectively for @Json names).
-keep class com.akilimo.mobile.entities.** { *; }
-keep enum com.akilimo.mobile.enums.** { *; }

# Classes annotated with @JsonClass(generateAdapter = true) use generated adapters looked up
# by name.
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepnames @com.squareup.moshi.JsonClass class *

# --- Navigation (type-safe routes) ----------------------------------------------------------
# @Serializable route objects/classes are looked up via kotlinx.serialization.
-keep @kotlinx.serialization.Serializable class com.akilimo.mobile.navigation.** { *; }
-keepclassmembers class com.akilimo.mobile.navigation.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# --- WorkManager ----------------------------------------------------------------------------
# Workers are instantiated by class name stored in the WorkManager database.
-keep class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}

# --- Misc -----------------------------------------------------------------------------------
# country_data looks up flag drawables by name.
-keep class com.blongho.country_data.** { *; }

-dontwarn org.slf4j.**
