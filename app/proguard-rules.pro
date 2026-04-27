# Keep kotlinx.serialization metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class io.bubblymarble.fitness.**$$serializer { *; }
-keepclassmembers class io.bubblymarble.fitness.** {
    *** Companion;
}
-keepclasseswithmembers class io.bubblymarble.fitness.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.GeneratedComponent { *; }

# Health Connect
-keep class androidx.health.** { *; }
