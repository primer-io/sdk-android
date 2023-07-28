package io.primer.android.components

import android.content.Context
import io.primer.android.data.settings.PrimerSettings

interface PrimerHeadlessUniversalCheckoutInterface {
    /**
     * Sets the [PrimerHeadlessUniversalCheckoutListener]
     * Use the listener in order to receive different events from the SDK.
     */
    fun setCheckoutListener(
        listener: PrimerHeadlessUniversalCheckoutListener
    )

    /**
     * Sets the [PrimerHeadlessUniversalCheckoutUiListener]
     * Use the listener in order to receive different UI events from the SDK
     */
    fun setCheckoutUiListener(uiListener: PrimerHeadlessUniversalCheckoutUiListener)

    /**
     * Starts the PrimerHeadlessUniversalCheckout SDK with [Context] & [clientToken] &
     * [PrimerSettings] & [PrimerHeadlessUniversalCheckoutListener] &
     * [PrimerHeadlessUniversalCheckoutUiListener].
     * Calling this function will reset any old state
     * @param clientToken base64 string containing information about this Primer session.
     * It expires after 24 hours. An expired client token will throw an [IllegalArgumentException].
     */
    fun start(
        context: Context,
        clientToken: String,
        settings: PrimerSettings? = null,
        checkoutListener: PrimerHeadlessUniversalCheckoutListener? = null,
        uiListener: PrimerHeadlessUniversalCheckoutUiListener? = null
    )

    /**
     * This method should be called when disposing the listener in order to free
     * PrimerHeadlessUniversalCheckout SDK resource.
     * Once instance of [PrimerHeadlessUniversalCheckout] has freed up the used resources,
     * it is in the same state as newly created [PrimerHeadlessUniversalCheckout] and can be used once again,
     * but should go through [start] once again.
     */
    fun cleanup()
}
