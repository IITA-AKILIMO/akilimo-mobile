# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class fertilizerName to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file fertilizerName.
#-renamesourcefileattribute SourceFile

#-keep class com.getkeepsafe.relinker.** { *; }

# Keep Jackson annotations
-keep class com.fasterxml.jackson.databind.ObjectMapper
-keep class com.fasterxml.jackson.databind.ObjectWriter
-keep class com.fasterxml.jackson.databind.ObjectReader
-keep @com.fasterxml.jackson.annotation.JsonCreator class *
-keep @com.fasterxml.jackson.annotation.JsonProperty class *
-keepclassmembers public class * {
    @com.fasterxml.jackson.annotation.JsonCreator *;
    @com.fasterxml.jackson.annotation.JsonProperty *;
}

# Keep classes related to Java 8 features if desugaring isn't fully covering it
# This might be needed if you see BootstrapMethodError specifically
-dontwarn java.lang.invoke.*
-keep class java.lang.invoke.** { *; }