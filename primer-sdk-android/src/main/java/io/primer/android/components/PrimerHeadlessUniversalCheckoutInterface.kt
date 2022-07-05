package io.primer.android.components

import android.content.Context
import android.view.View
import io.primer.android.ExperimentalPrimerApi
import io.primer.android.components.ui.widgets.elements.PrimerInputElementType
import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import io.primer.android.data.settings.PrimerSettings

@ExperimentalPrimerApi
interface PrimerHeadlessUniversalCheckoutInterface {
    /**
     * Sets the [PrimerHeadlessUniversalCheckoutListener]
     * Use the listener in order to receive different events from the SDK
     */
    fun setListener(listener: PrimerHeadlessUniversalCheckoutListener)

    /**
     * Starts the PrimerHeadlessUniversalCheckout SDK with [Context] & [clientToken] &
     * [PrimerSettings] & [PrimerHeadlessUniversalCheckoutListener].
     * Calling this function will reset any old state
     * @param clientToken base64 string containing information about this Primer session.
     * It expires after 24 hours. An expired client token will throw an [IllegalArgumentException].
     */
    fun start(
        context: Context,
        clientToken: String,
        settings: PrimerSettings? = null,
        listener: PrimerHeadlessUniversalCheckoutListener? = null
    )

    /**
     * Lists the [PrimerInputElementType] for a given [PrimerPaymentMethodType]
     */
    fun listRequiredInputElementTypes(paymentMethodType: PrimerPaymentMethodType):
        List<PrimerInputElementType>?

    /**
     * Creates [View] for a given [PrimerPaymentMethodType]
     */
    fun makeView(paymentMethodType: PrimerPaymentMethodType): View?

    /**
     * Initialise and show specific payment method flows with PrimerHeadlessUniversalCheckout.
     *
     * @param paymentMethodType the payment method flow to be shown.
     */
    fun showPaymentMethod(context: Context, paymentMethodType: PrimerPaymentMethodType)

    /**
     * This method should be called when disposing the listener in order to free
     * PrimerHeadlessUniversalCheckout SDK resource.
     * Once instance of [PrimerHeadlessUniversalCheckout] has freed up the used resources,
     * it is in the same state as newly created [PrimerHeadlessUniversalCheckout] and can be used once again,
     * but should go through [start] once again.
     */
    fun cleanup()
}
