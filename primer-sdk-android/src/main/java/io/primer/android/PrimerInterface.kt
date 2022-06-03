package io.primer.android

import android.content.Context
import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import io.primer.android.data.settings.PrimerSettings

interface PrimerInterface {

    /**
     * Configures the Primer SDK with [PrimerSettings] & [PrimerCheckoutListener].
     */
    fun configure(
        settings: PrimerSettings? = null,
        listener: PrimerCheckoutListener? = null,
    )

    /**
     * This method should be called when disposing the listener in order to free Primer SDK resource.
     * Once instance of [Primer] has freed up the used resources,
     * it is in the same state as newly created [Primer] and can be used once again,
     * but should go through [configure] once again.
     */
    fun cleanup()

    /**
     * Initialise and show Primer's Universal Checkout with all configured payment methods.
     *
     * @param clientToken base64 string containing information about this Primer session.
     * It expires after 24 hours. An expired client token will throw an [IllegalArgumentException].
     */
    fun showUniversalCheckout(context: Context, clientToken: String)

    /**
     * Initialise and show Primer's Vault Manager.
     *
     * @param clientToken base64 string containing information about this Primer session.
     * It expires after 24 hours. An expired client token will throw an [IllegalArgumentException].
     */
    fun showVaultManager(context: Context, clientToken: String)

    /**
     * Initialise and show specific payment method flows with Primer.
     *
     * @param clientToken base64 string containing information about this Primer session.
     * It expires after 24 hours. An expired client token will throw an [IllegalArgumentException].
     * @param paymentMethod the payment method flow to be shown.
     * @param intent whether to trigger checkout or vault session.
     */
    fun showPaymentMethod(
        context: Context,
        clientToken: String,
        paymentMethod: PrimerPaymentMethodType,
        intent: PrimerSessionIntent,
    )

    /**
     * Dismiss the checkout
     */
    fun dismiss(clearListeners: Boolean = false)
}
