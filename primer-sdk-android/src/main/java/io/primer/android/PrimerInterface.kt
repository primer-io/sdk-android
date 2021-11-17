package io.primer.android

import android.content.Context
import io.primer.android.model.PrimerDebugOptions
import io.primer.android.model.dto.CountryCode
import io.primer.android.model.dto.Customer
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.PrimerPaymentMethod
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType
import java.util.Locale

interface PrimerInterface {

    /**
     * Configures the Primer SDK with [PrimerConfig] & [CheckoutEventListener].
     */
    fun configure(
        config: PrimerConfig? = null,
        listener: CheckoutEventListener? = null,
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
        paymentMethod: PrimerPaymentMethod,
        intent: PaymentMethodIntent,
    )

    /**
     * Public method query Primer Vault for saved payment instrument tokens of a given user.
     * Ensure customer ID was specified in the client token creation API request before
     * calling this method.
     *
     * @param clientToken base64 string containing information about this Primer session.
     * It expires after 24 hours. An expired client token will throw an [IllegalArgumentException].
     */
    @Deprecated("This method is deprecated and will be removed in next release.")
    fun fetchSavedPaymentInstruments(clientToken: String)

    /**
     * Show a success screen then dismiss
     */
    fun showSuccess(autoDismissDelay: Int = 3000, successType: SuccessType = SuccessType.DEFAULT)

    /**
     * Show a error screen then dismiss
     */
    fun showError(autoDismissDelay: Int = 3000, errorType: ErrorType = ErrorType.DEFAULT)

    /**
     * Dismiss the checkout
     */
    fun dismiss(clearListeners: Boolean = false)

    /**
     * Load the provided payment methods for use with the SDK
     */
    @Deprecated("This method is deprecated.")
    fun loadPaymentMethods(paymentMethods: List<PaymentMethod>)

    /**
     * Public method query Primer Vault for saved payment instrument tokens of a given user.
     * Ensure customer ID was specified in the client token creation API request before
     * calling this method.
     *
     * @param callback this callback will be invoked after the payment instrument tokens call completes
     */
    @Deprecated(
        "This method is deprecated.",
        ReplaceWith("fetchSavedPaymentInstruments")
    )
    fun getSavedPaymentMethods(callback: (List<PaymentMethodToken>) -> Unit)

    /**
     * Initializes the Primer SDK with the Application context and a client token Provider
     *
     * @param clientToken base64 string containing information about this Primer session.
     * It expires after 24 hours. Passing in an expired client token will throw an [IllegalArgumentException].
     */

    @Throws(IllegalArgumentException::class)
    @Deprecated("This method is deprecated.")
    fun initialize(
        context: Context,
        clientToken: String,
        locale: Locale = Locale.getDefault(),
        countryCode: CountryCode? = null,
        theme: PrimerTheme? = null,
    )

    @Deprecated(
        "This method is deprecated.",
        ReplaceWith("showVaultManager"),
    )
    fun showVault(
        context: Context,
        listener: CheckoutEventListener,
        amount: Int? = null,
        currency: String? = null,
        webBrowserRedirectScheme: String? = null,
        isStandalonePaymentMethod: Boolean = false,
        doNotShowUi: Boolean = false,
        preferWebView: Boolean = false,
        is3DSOnVaultingEnabled: Boolean = false,
        debugOptions: PrimerDebugOptions? = null,
        orderId: String? = null,
        customer: Customer? = null,
        clearAllListeners: Boolean = false,
    )

    @Deprecated(
        "This method is deprecated.",
        ReplaceWith("showUniversalCheckout"),
    )
    fun showCheckout(
        context: Context,
        listener: CheckoutEventListener,
        amount: Int? = null,
        currency: String? = null,
        webBrowserRedirectScheme: String? = null,
        isStandalonePaymentMethod: Boolean = false,
        doNotShowUi: Boolean = false,
        preferWebView: Boolean = false,
        debugOptions: PrimerDebugOptions? = null,
        orderId: String? = null,
        customer: Customer? = null,
        clearAllListeners: Boolean = false,
    )

    @Deprecated("This method is deprecated")
    fun showProgressIndicator(visible: Boolean)
}
