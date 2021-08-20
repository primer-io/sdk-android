# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class org.bouncycastle.** { *; }
-keepnames class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Keep everyting in the Netcetera Android 3DS SDK package
-keep public class com.netcetera.threeds.sdk.** {
  public protected *;
}
-keepnames class com.netcetera.threeds.sdk.** { *; }

# Don't warn about any unused code from the Netcetera Android 3DS SDK package
-dontwarn com.netcetera.threeds.sdk.**

# Keep everyting in Guardsquare Dexguard
-keep public class com.guardsquare.dexguard.** {
  public protected *;
}

# Keep logback and slf4j used by Netcetera
-keep public class org.slf4j.** { *; }
-keep public class ch.** { *; }

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
