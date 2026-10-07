# Consumer rules for the Primer Nol Pay SDK.
#
# AGP merges this file into the R8 configuration of EVERY app that depends on the
# AAR, so each rule here must be scoped to this SDK's own classes. Never add global
# directives (-repackageclasses, -allowaccessmodification, ...) or unscoped keeps
# such as `-keep class * { ... }` here: they apply to the whole app, not the SDK.
# Rules that only concern this library's own release build go in proguard-rules.pro.

-dontwarn java.lang.invoke.StringConcatFactory
# Gson accesses sun.misc.Unsafe reflectively.
-dontwarn sun.misc.**

##---------------Begin: Nol Transit SDK (libs/classes.jar) ----------
# The bundled Transit SDK is a vendor-obfuscated black box that (de)serialises its
# models with Gson through reflection and TypeToken, so its classes must survive
# the app's R8 pass untouched.
-keep class com.snowballtech.** { *; }
##---------------End: Nol Transit SDK ----------

##---------------Begin: Gson (used by the Transit SDK) ----------
# Gson reads generic signatures and runtime annotations (@SerializedName) from the
# classes kept above, and R8 strips both attributes unless they are kept.
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# Gson populates fields by reflection, so R8 sees no writes and no constructor calls.
# These are Gson's own recommended rules (shipped inside Gson >= 2.11, absent in 2.10.x):
# keep @SerializedName fields on any class that has them, and keep that class's
# no-arg constructor so R8 does not treat the class as never instantiated.
-if class *
-keepclasseswithmembers,allowobfuscation class <1> {
  @com.google.gson.annotations.SerializedName <fields>;
}
-if class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers,allowobfuscation,allowoptimization class <1> {
  <init>();
}

# Retain the generic signature of TypeToken and its subclasses (R8 3.0+).
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
##---------------End: Gson ----------

##---------------Begin: Primer Nol Pay SDK ----------
# Let the app's R8 shrink unused SDK code, but never rename what survives, so crash
# reports show the same class and member names as this library's own mapping file.
-keepnames class io.primer.nolpay.** { *; }
##---------------End: Primer Nol Pay SDK ----------
