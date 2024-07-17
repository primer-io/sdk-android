# we need to keep class names for reflection used by DI
-keepnames class io.primer.android.**

# for the BuildConfig
-keep class io.primer.android.BuildConfig { *; }

# wrapper SDKs are added as compileOnly dependencies, and all of them contain specific proguard-rules.pro
# yet, when they are not included by merchant app, R8 will complain as there will be missing rules
-dontwarn com.klarna.mobile.sdk.api.**
-dontwarn io.primer.ipay88.api.**
-dontwarn io.primer.nolpay.api.**
-dontwarn com.snowballtech.transit.rta.configuration.TransitAppSecretKeyHandler
-dontwarn com.snowballtech.transit.rta.configuration.TransitConfiguration
-dontwarn com.netcetera.threeds.sdk.ThreeDS2ServiceInstance
-dontwarn com.netcetera.threeds.sdk.api.**

# Stripe ACH
-dontwarn io.primer.android.stripe.StripeBankAccountCollectorActivity$Companion
-dontwarn io.primer.android.stripe.StripeBankAccountCollectorActivity$Params
-dontwarn io.primer.android.stripe.StripeBankAccountCollectorActivity
-dontwarn io.primer.android.stripe.exceptions.StripePublishableKeyMismatchException
-dontwarn io.primer.android.stripe.exceptions.StripeSdkException
