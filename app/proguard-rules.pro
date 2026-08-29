# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# minifyEnabled is currently false for the release build type (see
# app/build.gradle.kts), so these rules are not applied yet — this
# file exists so that turning minification on later doesn't require
# creating it from scratch, and so tooling that expects the file to
# exist (Android Studio's release build wizard, some CI templates)
# doesn't fail looking for it.

# Keep Media3/ExoPlayer's reflective session classes.
-keep class androidx.media3.session.** { *; }
-keep class androidx.media3.exoplayer.** { *; }

# Keep Hilt-generated components.
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager

# Keep Room-generated classes.
-keep class androidx.room.** { *; }

# Kotlin coroutines internals sometimes referenced only via reflection.
-keepclassmembernames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
