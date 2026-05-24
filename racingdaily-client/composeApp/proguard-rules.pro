# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.racingdaily.**$$serializer { *; }
-keepclassmembers class com.racingdaily.** {
    *** Companion;
}
-keepclasseswithmembers class com.racingdaily.** {
    kotlinx.serialization.KSerializer serializer(...);
}
