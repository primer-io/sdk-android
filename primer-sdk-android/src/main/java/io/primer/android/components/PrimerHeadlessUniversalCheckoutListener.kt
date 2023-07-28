package io.primer.android.components

import io.primer.android.completion.PrimerHeadlessUniversalCheckoutResumeDecisionHandler
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData

@JvmDefaultWithCompatibility
interface PrimerHeadlessUniversalCheckoutListener {

    /**
     * Called after the [PrimerHeadlessUniversalCheckout] has been initialized.
     * Based on the clientToken provided, the payment methods that should be displayed will be returned.
     */
    fun onAvailablePaymentMethodsLoaded(
        paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>
    )

    /**
     * Called before the payment method tokenization is performed.
     */
    fun onTokenizationStarted(paymentMethodType: String) = Unit

    /**
     * Called when payment method tokenization is successful.
     * @param paymentMethodTokenData represents the tokenized payment method.
     * @param decisionHandler can be used to continue the payment flow in case there is required action needed.
     * Will only be called if [io.primer.android.data.settings.PrimerPaymentHandling] is set to
     * [io.primer.android.data.settings.PrimerPaymentHandling.MANUAL]
     * @see io.primer.android.data.settings.PrimerSettings
     */
    fun onTokenizeSuccess(
        paymentMethodTokenData: PrimerPaymentMethodTokenData,
        decisionHandler: PrimerHeadlessUniversalCheckoutResumeDecisionHandler
    ) = Unit

    /**
     * Called when payment method resume is successful.
     * @param resumeToken should be used to continue/complete the PENDING payment.
     * @param decisionHandler can be used to continue the payment flow in case there is required action needed.
     * Will only be called if [io.primer.android.data.settings.PrimerPaymentHandling] is set to
     * [io.primer.android.data.settings.PrimerPaymentHandling.MANUAL]
     * @see io.primer.android.data.settings.PrimerSettings
     */
    fun onCheckoutResume(
        resumeToken: String,
        decisionHandler: PrimerHeadlessUniversalCheckoutResumeDecisionHandler
    ) = Unit

    /**
     * Called when there is additional information returned for a payment. These can be used
     * to show information to the users in your own UI.
     * At this stage the checkout flow can be considered as completed.
     * @param additionalInfo should be used to get additional payment info.
     * Will only be called if [io.primer.android.data.settings.PrimerPaymentHandling] is set to
     * [io.primer.android.data.settings.PrimerPaymentHandling.MANUAL]
     * @see io.primer.android.data.settings.PrimerSettings
     */
    fun onResumePending(additionalInfo: PrimerCheckoutAdditionalInfo) = Unit

    /**
     * Called in case SDK encounters an error.
     * @param error [io.primer.android.domain.error.models.PrimerError].
     * Will only be called if [io.primer.android.data.settings.PrimerPaymentHandling] is set to
     * [io.primer.android.data.settings.PrimerPaymentHandling.MANUAL]
     * @see io.primer.android.data.settings.PrimerSettings
     */
    fun onFailed(error: PrimerError) = Unit

    /**
     * Called before the tokenization and payment creation has started.
     * @param paymentMethodData represents a simple data about the payment method.
     * @param createPaymentHandler can be used to continue/cancel tokenization and payment process at this stage.
     * Will only be called if [io.primer.android.data.settings.PrimerPaymentHandling] is set to
     * [io.primer.android.data.settings.PrimerPaymentHandling.AUTO]
     * @see io.primer.android.data.settings.PrimerSettings
     */
    fun onBeforePaymentCreated(
        paymentMethodData: PrimerPaymentMethodData,
        createPaymentHandler: PrimerPaymentCreationDecisionHandler
    ) {
        createPaymentHandler.continuePaymentCreation()
    }

    /**
     * Called when there is additional information returned for a payment. These can be used
     * to show information to the users in your own UI.
     * @param additionalInfo should be used to get additional payment info.
     * Will only be called if [io.primer.android.data.settings.PrimerPaymentHandling] is set to
     * [io.primer.android.data.settings.PrimerPaymentHandling.AUTO]
     * @see io.primer.android.data.settings.PrimerSettings
     */
    fun onCheckoutAdditionalInfoReceived(additionalInfo: PrimerCheckoutAdditionalInfo) = Unit

    /**
     * Called when the user completed the checkout.
     * @param checkoutData represents a simple model about completed payment.
     * Will be called only if [io.primer.android.data.settings.PrimerPaymentHandling] is set to
     * [io.primer.android.data.settings.PrimerPaymentHandling.AUTO]
     * @see io.primer.android.data.settings.PrimerSettings
     */
    fun onCheckoutCompleted(checkoutData: PrimerCheckoutData)

    fun onBeforeClientSessionUpdated() = Unit

    fun onClientSessionUpdated(clientSession: PrimerClientSession) = Unit

    /**
     * Called in case SDK encounters an error.
     * @param error [io.primer.android.domain.error.models.PrimerError].
     * @param checkoutData represents a simple model about failed payment.
     * Will be called only if [io.primer.android.data.settings.PrimerPaymentHandling] is set to
     * [io.primer.android.data.settings.PrimerPaymentHandling.AUTO]
     * @see io.primer.android.data.settings.PrimerSettings
     */
    fun onFailed(error: PrimerError, checkoutData: PrimerCheckoutData? = null) = Unit
}
