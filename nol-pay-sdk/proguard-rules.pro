# R8 configuration for this library's own release build ONLY.
#
# Everything a consuming app needs lives in consumer-rules.pro, which is shipped
# inside the AAR and is also applied to this build (see build.gradle). Keep the two
# apart: the global directives below must never reach consuming apps.

-dontusemixedcaseclassnames
-allowaccessmodification
-repackageclasses 'io.primer.nolpay.internal'
-renamesourcefileattribute SourceFile
-keepattributes Signature,Exceptions,*Annotation*,
                InnerClasses,PermittedSubclasses,EnclosingMethod,
                Deprecated,SourceFile,LineNumberTable

##---------------Begin: Primer Nol Pay SDK public API ----------

-keep class io.primer.nolpay.api.PrimerNolPay { *; }

-keep class io.primer.nolpay.api.PrimerNolPayNfcUtils { *; }

-keep class io.primer.nolpay.api.models.* { public *; }
-keep class io.primer.nolpay.api.exceptions.* { public *; }

##---------------End: Primer Nol Pay SDK public API ----------
