package io.primer.android.googlepay.implementation.configuration.domain.model

import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.data.settings.PrimerGoogleShippingAddressParameters
import io.primer.android.googlepay.GooglePayFacade
import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration

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
    val existingPaymentMethodRequired: Boolean,
    val shippingOptions: CheckoutModule.Shipping?,
    val shippingAddressParameters: PrimerGoogleShippingAddressParameters? = null,
    val requireShippingMethod: Boolean,
    val emailAddressRequired: Boolean,
) : PaymentMethodConfiguration
