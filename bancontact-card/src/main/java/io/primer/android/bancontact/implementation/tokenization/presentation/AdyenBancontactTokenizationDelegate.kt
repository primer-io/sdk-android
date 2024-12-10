package io.primer.android.bancontact.implementation.tokenization.presentation

import io.primer.android.bancontact.implementation.configuration.domain.AdyenBancontactConfigurationInteractor
import io.primer.android.bancontact.implementation.configuration.domain.model.AdyenBancontactConfigParams
import io.primer.android.bancontact.implementation.tokenization.domain.AdyenBancontactTokenizationInteractor
import io.primer.android.bancontact.implementation.tokenization.domain.model.AdyenBancontactPaymentInstrumentParams
import io.primer.android.bancontact.implementation.tokenization.presentation.composable.AdyenBancontactTokenizationInputable
import io.primer.android.core.domain.None
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper
import io.primer.android.webRedirectShared.implementation.deeplink.domain.RedirectDeeplinkInteractor

internal class AdyenBancontactTokenizationDelegate(
    private val configurationInteractor: AdyenBancontactConfigurationInteractor,
    private val deeplinkInteractor: RedirectDeeplinkInteractor,
    tokenizationInteractor: AdyenBancontactTokenizationInteractor
) : PaymentMethodTokenizationDelegate<AdyenBancontactTokenizationInputable, AdyenBancontactPaymentInstrumentParams>(
    tokenizationInteractor
),
    TokenizationCollectedDataMapper<AdyenBancontactTokenizationInputable, AdyenBancontactPaymentInstrumentParams> {

    override suspend fun mapTokenizationData(input: AdyenBancontactTokenizationInputable) =
        configurationInteractor(AdyenBancontactConfigParams(paymentMethodType = input.paymentMethodType))
            .map { configuration ->
                TokenizationParams(
                    AdyenBancontactPaymentInstrumentParams(
                        paymentMethodType = input.paymentMethodType,
                        number = input.cardData.cardNumber,
                        expirationMonth = input.cardData.expiryDate.split("/").first().padStart(
                            EXPIRATION_MONTH_PAD_START_LENGTH,
                            EXPIRATION_MONTH_PAD_START_CHAR
                        ),
                        expirationYear = input.cardData.expiryDate.split("/")[1],
                        cardholderName = input.cardData.cardHolderName,
                        paymentMethodConfigId = configuration.paymentMethodConfigId,
                        locale = configuration.locale.toString(),
                        userAgent = System.getProperty("http.agent").orEmpty(),
                        redirectionUrl = deeplinkInteractor(None)

                    ),
                    input.primerSessionIntent
                )
            }

    private companion object {
        private const val EXPIRATION_MONTH_PAD_START_LENGTH = 2
        private const val EXPIRATION_MONTH_PAD_START_CHAR = '0'
    }
}
