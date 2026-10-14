-dontusemixedcaseclassnames
-allowaccessmodification
-repackageclasses 'io.primer.nolpay.internal'
-renamesourcefileattribute SourceFile
-keepattributes Signature,Exceptions,*Annotation*,
                InnerClasses,PermittedSubclasses,EnclosingMethod,
                Deprecated,SourceFile,LineNumberTable

-keep class io.primer.nolpay.api.PrimerNolPay { *; }
-keep class io.primer.nolpay.api.PrimerNolPayNfcUtils { *; }
-keep class io.primer.nolpay.api.models.* { public *; }
-keep class io.primer.nolpay.api.exceptions.* { public *; }
