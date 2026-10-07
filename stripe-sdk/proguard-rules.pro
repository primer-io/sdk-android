-repackageclasses 'io.primer.android.stripe.internal'

-keep class io.primer.android.stripe.model.StripeAchBank { *; }
-keep class io.primer.android.stripe.StripeBankAccountCollectorActivity { *; }
-keep class io.primer.android.stripe.StripeBankAccountCollectorActivity$Params { *; }
-keep class io.primer.android.stripe.StripeBankAccountCollectorActivity$Companion { *; }
-keep class io.primer.android.stripe.exceptions.* { public *; }

-keep class com.stripe.** { *; }
-dontwarn java.lang.invoke.StringConcatFactory
