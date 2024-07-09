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

-dontwarn java.lang.invoke.StringConcatFactory

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

-keep class io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod {
    *;
}

-keep class io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethodData* {
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

-keep enum io.primer.android.threeds.data.models.common.ResponseCode

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

-keep public interface io.primer.android.data.payments.configure.PrimerInitializationData {
    public *;
}

-keep class io.primer.android.data.payments.configure.** {
    *;
}

-keep class io.primer.android.domain.rpc.retailOutlets.models.* {
    *;
}

-keep class io.primer.android.domain.rpc.banks.models.* {
    public *;
}

-keep class io.primer.android.components.domain.core.models.retailOutlet.PrimerRetailerData {
    *;
}

-keep class io.primer.android.domain.error.models.PrimerError {
    public *;
}

 #------------------------------------HUC UI------------------------------------------------------#

-keep class io.primer.android.components.domain.core.models.** {
    public *;
}

-keep interface io.primer.android.components.manager.* {
    *;
}

-keep class io.primer.android.components.ui.views.* {
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

-keep interface io.primer.android.components.PrimerHeadlessUniversalCheckoutUiListener {
    public *;
}

 #------------------------------------HUC raw---------------------------------------------------#
-keep class io.primer.android.components.manager.raw.* {
    public *;
}

-keep class io.primer.android.components.domain.error.* {
    *;
}

-keep enum io.primer.android.components.domain.inputs.models.PrimerInputElementType {
    *;
}

 #------------------------------------HUC native---------------------------------------------------#
-keep class io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManager {
    public *;
}

-keep class io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManager$Companion {
    *;
}

-keep interface io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManagerInterface {
    public *;
}

 #------------------------------------HUC assets---------------------------------------------------#
-keep class io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager {
    public *;
}

-keep class io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager$Companion {
    *;
}

-keep class io.primer.android.components.ui.assets.PrimerPaymentMethodAsset {
    public *;
}

-keep class io.primer.android.components.ui.assets.PrimerPaymentMethodLogo {
    public *;
}

-keep class io.primer.android.components.ui.assets.PrimerPaymentMethodBackgroundColor {
    public *;
}

-keep interface io.primer.android.components.ui.assets.PrimerAsset {
    *;
}

-keep class io.primer.android.components.ui.assets.PrimerCardNetworkAsset {
   public *;
}

 #------------------------------------HUC exceptions-----------------------------------------------#

-keep class io.primer.android.components.SdkUninitializedException {
    public *;
}

-keep class io.primer.android.components.domain.exception.UnsupportedPaymentMethodManagerException {
    public *;
}

-keep class io.primer.android.domain.exception.UnsupportedPaymentMethodException {
    public *;
}

-keep class io.primer.android.domain.exception.UnsupportedPaymentIntentException {
    public *;
}

 #------------------------------------Vault manager------------------------------------------------#

-keep class io.primer.android.components.manager.vault.PrimerHeadlessUniversalCheckoutVaultManager {
    public *;
}

-keep class io.primer.android.components.manager.vault.* {
    *;
}

-keep interface io.primer.android.components.domain.payments.vault.PrimerVaultedPaymentMethodAdditionalData {
    public *;
}

-keep class io.primer.android.components.domain.payments.vault.model.** {
    public *;
}

-keep class io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod {
    public *;
}

-keep class io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod$AuthenticationDetails {
    public *;
}

 #------------------------------------Vault manager exceptions-------------------------------------#

-keep class io.primer.android.components.domain.exception.* {
    public *;
}

 #------------------------------------Headless components------------------------------------------------#

-keep class io.primer.android.components.manager.core.** {
    public *;
}

-keep class io.primer.android.components.manager.klarna.* {
    public *;
}

-keep class io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.* {
    public *;
}

-keep class io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.composable.** {
    public *;
}

-keep class io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.** {
    public *;
}

-keep class io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.** {
    public *;
}

-keep class io.primer.android.components.manager.componentWithRedirect.* {
    public *;
}

-keep class io.primer.android.components.manager.componentWithRedirect.composable.** {
    public *;
}

-keep class io.primer.android.components.manager.componentWithRedirect.component.** {
    public *;
}

-keep class io.primer.android.components.manager.banks.component.* {
    *;
}

-keep class io.primer.android.components.manager.banks.composable.* {
    *;
}

-keep class io.primer.android.components.manager.nolPay.core.** {
    public *;
}

-keep class io.primer.android.components.manager.nolPay.linkCard.component.* {
    *;
}

-keep class io.primer.android.components.manager.nolPay.linkCard.composable.* {
    *;
}

-keep class io.primer.android.components.manager.nolPay.unlinkCard.component.* {
    *;
}

-keep class io.primer.android.components.manager.nolPay.unlinkCard.composable.* {
    *;
}

-keep class io.primer.android.components.manager.nolPay.payment.component.* {
    *;
}

-keep class io.primer.android.components.manager.nolPay.payment.composable.* {
    *;
}

-keep class io.primer.android.components.manager.nolPay.nfc.** {
    *;
}

-keep class io.primer.android.components.manager.nolPay.listCards.** {
    *;
}

-keep class io.primer.android.components.manager.nolPay.PrimerHeadlessUniversalCheckoutNolPayManager {
    *;
}

 #------------------------------------Exceptions--------------------------------------------------#

-keep class io.primer.android.components.domain.payments.metadata.phone.exception.* {
    public *;
}

 #------------------------------------Logging---------------------------------------------------#

-keep class io.primer.android.core.logging.PrimerLog {
    public *;
}

-keep class io.primer.android.core.logging.PrimerLogging {
    public *;
}

-keep class io.primer.android.core.logging.PrimerLogger {
    public *;
}

-keep enum io.primer.android.core.logging.PrimerLogLevel {
    public *;
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

# for the BuildConfig
-keep class io.primer.android.BuildConfig { *; }

-keepattributes RuntimeVisibleAnnotations