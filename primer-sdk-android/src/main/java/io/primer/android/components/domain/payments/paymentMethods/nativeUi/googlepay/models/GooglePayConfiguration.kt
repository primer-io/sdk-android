package io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.models

import io.primer.android.data.settings.PrimerGoogleShippingAddressParameters
import io.primer.android.domain.session.models.CheckoutModule
import io.primer.android.payment.google.GooglePayFacade

internal data class GooglePayConfiguration(
    val environment: GooglePayFacade.Environment,
    val gatewayMerchantId: String,
    val merchantName: String?,
    val totalPrice: String,
    val countryCode: String,
    val currencyCode: String,
    val allowedCardNetworks: List<String>,
    val allowedCardAuthMethods: List<String>,
    val billingAddressRequired: Boolean,
    val shippingOptions: CheckoutModule.Shipping?,
    val shippingAddressParameters: PrimerGoogleShippingAddressParameters? = null,
    val requireShippingMethod: Boolean,
    val emailAddressRequired: Boolean
)
