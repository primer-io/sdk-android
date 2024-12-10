package io.primer.android.googlepay.implementation.composer.ui.navigation.launcher

import io.primer.android.PrimerSessionIntent
import io.primer.android.data.settings.PrimerGoogleShippingAddressParameters
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodRedirectLauncherParams
import io.primer.android.googlepay.GooglePayFacade

internal class GooglePayActivityLauncherParams(
    val environment: GooglePayFacade.Environment,
    val gatewayMerchantId: String,
    val merchantName: String? = null,
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
) : PaymentMethodRedirectLauncherParams(
    PaymentMethodType.GOOGLE_PAY.name,
    PrimerSessionIntent.CHECKOUT
)
