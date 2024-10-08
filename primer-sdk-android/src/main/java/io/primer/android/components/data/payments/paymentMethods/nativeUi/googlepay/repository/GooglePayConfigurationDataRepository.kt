package io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.exception.GooglePayIllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.models.GooglePayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.repository.GooglePayConfigurationRepository
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.Environment
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.session.models.CheckoutModule
import io.primer.android.domain.session.models.findFirstInstance
import io.primer.android.payment.google.GooglePay
import io.primer.android.payment.google.GooglePayFacade
import io.primer.android.utils.PaymentUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Currency

internal class GooglePayConfigurationDataRepository(
    private val settings: PrimerSettings,
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : GooglePayConfigurationRepository {
    override fun getConfiguration(): Flow<GooglePayConfiguration> {
        return flow {
            val paymentMethodConfig =
                localConfigurationDataSource.getConfiguration().paymentMethods
                    .first { it.type == PaymentMethodType.GOOGLE_PAY.name }
            val shippingOptions = localConfigurationDataSource.getConfiguration().toConfiguration()
                .checkoutModules.findFirstInstance<CheckoutModule.Shipping>()
            val allowedCardNetworks = localConfigurationDataSource.getConfiguration().clientSession
                .paymentMethod?.orderedAllowedCardNetworks.orEmpty().intersect(
                    GooglePay.allowedCardNetworks
                ).toList().map { type -> type.name }
            val googlePayOptions = settings.paymentMethodOptions.googlePayOptions
            emit(
                GooglePayConfiguration(
                    environment = getGooglePayEnvironment(
                        localConfigurationDataSource.getConfiguration().environment
                    ),
                    gatewayMerchantId = requireNotNullCheck(
                        paymentMethodConfig.options?.merchantId,
                        GooglePayIllegalValueKey.MERCHANT_ID
                    ),
                    merchantName = googlePayOptions.merchantName,
                    totalPrice = PaymentUtils.minorToAmount(
                        settings.currentAmount,
                        Currency.getInstance(settings.currency)
                    ).toString(),
                    countryCode = settings.order.countryCode.toString(),
                    currencyCode = settings.currency,
                    allowedCardNetworks = allowedCardNetworks,
                    allowedCardAuthMethods = allowedCardAuthMethods,
                    billingAddressRequired = googlePayOptions.captureBillingAddress,
                    shippingOptions = shippingOptions,
                    shippingAddressParameters = googlePayOptions.shippingAddressParameters,
                    requireShippingMethod = googlePayOptions.requireShippingMethod,
                    emailAddressRequired = googlePayOptions.emailAddressRequired
                )
            )
        }
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
