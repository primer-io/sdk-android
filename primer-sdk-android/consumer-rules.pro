-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class io.primer.android.**$$serializer { *; }
-keepclassmembers class io.primer.android.** {
    *** Companion;
}
-keepclasseswithmembers class io.primer.android.** {
    kotlinx.serialization.KSerializer serializer(...);
}