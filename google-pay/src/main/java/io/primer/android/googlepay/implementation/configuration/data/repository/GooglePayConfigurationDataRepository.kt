package io.primer.android.googlepay.implementation.configuration.data.repository

import io.primer.android.data.settings.PrimerSettings
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.Environment
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.configuration.domain.model.findFirstInstance
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.googlepay.GooglePayFacade
import io.primer.android.googlepay.implementation.configuration.domain.model.GooglePayConfiguration
import io.primer.android.googlepay.implementation.errors.data.exception.GooglePayIllegalValueKey
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.configuration.domain.model.NoOpPaymentMethodConfigurationParams
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.utils.PaymentUtils
import java.util.Currency

internal class GooglePayConfigurationDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
    private val settings: PrimerSettings
) : PaymentMethodConfigurationRepository<GooglePayConfiguration, NoOpPaymentMethodConfigurationParams> {

    override fun getPaymentMethodConfiguration(params: NoOpPaymentMethodConfigurationParams) =
        runSuspendCatching {
            val paymentMethodConfig =
                configurationDataSource.get().paymentMethods
                    .first { it.type == PaymentMethodType.GOOGLE_PAY.name }

            val order = requireNotNull(configurationDataSource.get().clientSession.order)
            val allowedCardNetworks = configurationDataSource.get().clientSession
                .paymentMethod?.orderedAllowedCardNetworks.orEmpty().intersect(
                    io.primer.android.googlepay.GooglePay.allowedCardNetworks
                ).toList().map { type -> type.name }

            val shippingOptions = configurationDataSource.get().toConfiguration()
                .checkoutModules.findFirstInstance<CheckoutModule.Shipping>()

            val googlePayOptions = settings.paymentMethodOptions.googlePayOptions

            GooglePayConfiguration(
                environment = getGooglePayEnvironment(configurationDataSource.get().environment),
                gatewayMerchantId = requireNotNullCheck(
                    paymentMethodConfig.options?.merchantId,
                    GooglePayIllegalValueKey.MERCHANT_ID
                ),
                merchantName = googlePayOptions.merchantName,
                totalPrice = PaymentUtils.minorToAmount(
                    order.currentAmount,
                    Currency.getInstance(order.currencyCode)
                ).toString(),
                countryCode = order.countryCode.toString(),
                currencyCode = order.currencyCode.orEmpty(),
                allowedCardNetworks = allowedCardNetworks,
                allowedCardAuthMethods = allowedCardAuthMethods,
                billingAddressRequired = googlePayOptions.captureBillingAddress,
                existingPaymentMethodRequired =
                googlePayOptions.existingPaymentMethodRequired,
                shippingOptions = shippingOptions,
                shippingAddressParameters = googlePayOptions.shippingAddressParameters,
                requireShippingMethod = googlePayOptions.requireShippingMethod,
                emailAddressRequired = googlePayOptions.emailAddressRequired
            )
        }

    private fun getGooglePayEnvironment(environment: Environment) =
        if (environment == Environment.PRODUCTION) {
            GooglePayFacade.Environment.PRODUCTION
        } else {
            GooglePayFacade.Environment.TEST
        }

    private companion object {
        val allowedCardAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
    }
}
