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

-dontusemixedcaseclassnames
-repackageclasses 'io.primer.android.internal'
-allowaccessmodification
-renamesourcefileattribute SourceFile
-keepattributes Signature,Exceptions,*Annotation*,
                InnerClasses,PermittedSubclasses,EnclosingMethod,
                Deprecated,SourceFile,LineNumberTable


-keep public class io.primer.android.**settings.** {
    public *;
}

-keep enum io.primer.android.PrimerSessionIntent {
    public *;
}

-keep class io.primer.android.Primer {
    public protected static <methods>;
    *** Companion;
}

-keep class io.primer.android.Primer$Companion {
    *;
}

-keep public interface io.primer.android.PrimerInterface {
    public *;
}

-keep public interface io.primer.android.PrimerCheckoutListener {
    public *;
}
-keep class io.primer.android.domain.action.models.* {
    public *;
}

-keep interface io.primer.android.completion.* {
    public *;
}

-keep class io.primer.android.domain.tokenization.models.PrimerPaymentMethodData {
    public *;
}

-keep class io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData {
    *;
}

-keep class io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData$* {
    *;
}

-keep class io.primer.android.data.tokenization.models.PaymentInstrumentData {
    *;
}

-keep class io.primer.android.data.tokenization.models.ExternalPayerInfo {
    *;
}

-keep class io.primer.android.data.tokenization.models.SessionData {
    *;
}

-keep class io.primer.android.data.tokenization.models.BillingAddress {
    *;
}

-keep  enum io.primer.android.threeds.data.models.ResponseCode

-keep class io.primer.android.data.tokenization.models.BinData {
    *;
}

-keep enum io.primer.android.data.tokenization.models.TokenType {
    *;
}

-keep class io.primer.android.domain.PrimerCheckoutData {
    *;
}

-keep class io.primer.android.domain.payments.create.model.Payment {
    *;
}

-keep class io.primer.android.domain.payments.additionalInfo.* {
    *;
}

-keep enum io.primer.android.data.configuration.models.CountryCode {
    *;
}

-keep enum io.primer.android.data.configuration.models.PaymentMethodType {
    public *;
    *** Companion;
}

-keep class io.primer.android.data.configuration.models.PaymentMethodType$Companion {
    *;
}

-keep public interface io.primer.android.data.payments.configure.PrimerInitializationData {
    public *;
}

-keep class io.primer.android.data.payments.configure.** {
    *;
}

-keep class io.primer.android.domain.rpc.retailOutlets.models.* {
    *;
}

-keep class io.primer.android.components.domain.core.models.retailOutlet.PrimerRawRetailerData {
    *;
}

-keep class io.primer.android.domain.error.models.PrimerError {
    public *;
}

 #------------------------------------HUC UI------------------------------------------------------#

-keep class io.primer.android.components.domain.core.models.** {
    public *;
}

-keep class io.primer.android.components.manager.PrimerCardManager {
    public *;
}

-keep interface io.primer.android.components.manager.* {
    *;
}

-keep class io.primer.android.components.manager.PrimerCardManager$Companion {
    *;
}

-keep class io.primer.android.components.manager.PrimerBancontactCardManager$Companion {
    *;
}

-keep interface io.primer.android.components.ui.widgets.** {
    *;
}

-keep class io.primer.android.components.ui.widgets.** {
    *;
}

-keep class io.primer.android.components.ui.views.* {
    *;
}

-keep enum io.primer.android.components.ui.assets.ImageType {
    *;
}

-keep enum io.primer.android.components.ui.assets.ImageColor {
    *;
}

-keep class io.primer.android.ui.CardNetwork {
    *;
}

-keep enum io.primer.android.ui.CardNetwork$Type {
    *;
}
 #------------------------------------HUC main---------------------------------------------------#
-keep class io.primer.android.components.PrimerHeadlessUniversalCheckout {
    public *;
}

-keep class io.primer.android.components.PrimerHeadlessUniversalCheckout$Companion {
    *;
}

-keep interface io.primer.android.components.PrimerHeadlessUniversalCheckoutInterface {
    public *;
}

-keep interface io.primer.android.components.PrimerHeadlessUniversalCheckoutListener {
    public *;
}

 #------------------------------------HUC raw---------------------------------------------------#
-keep class io.primer.android.components.manager.raw.* {
    public *;
}

-keep class io.primer.android.components.domain.error.PrimerInputValidationError {
    *;
}
-keep enum  io.primer.android.components.domain.inputs.models.PrimerInputElementType {
    *;
}

-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# for annotations
-keep @interface io.primer.android.ExperimentalPrimerApi
