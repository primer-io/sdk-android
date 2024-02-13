package io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.exception.GooglePayIllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.models.GooglePayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.repository.GooglePayConfigurationRepository
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.Environment
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
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
            val allowedCardNetworks = localConfigurationDataSource.getConfiguration().clientSession
                .paymentMethod?.orderedAllowedCardNetworks.orEmpty().intersect(
                    GooglePay.allowedCardNetworks
                ).toList().map { type -> type.name }
            emit(
                GooglePayConfiguration(
                    getGooglePayEnvironment(
                        localConfigurationDataSource.getConfiguration().environment
                    ),
                    requireNotNullCheck(
                        paymentMethodConfig.options?.merchantId,
                        GooglePayIllegalValueKey.MERCHANT_ID
                    ),
                    settings.paymentMethodOptions.googlePayOptions.merchantName,
                    PaymentUtils.minorToAmount(
                        settings.currentAmount,
                        Currency.getInstance(settings.currency)
                    ).toString(),
                    settings.order.countryCode.toString(),
                    settings.currency,
                    allowedCardNetworks,
                    allowedCardAuthMethods,
                    settings.paymentMethodOptions.googlePayOptions.captureBillingAddress
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
