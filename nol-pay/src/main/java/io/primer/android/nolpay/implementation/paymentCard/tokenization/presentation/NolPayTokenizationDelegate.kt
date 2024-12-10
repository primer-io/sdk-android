package io.primer.android.nolpay.implementation.paymentCard.tokenization.presentation

import io.primer.android.core.extensions.zipWith
import io.primer.android.nolpay.implementation.configuration.domain.NolPayConfigurationInteractor
import io.primer.android.nolpay.implementation.configuration.domain.model.NolPayConfigParams
import io.primer.android.nolpay.implementation.paymentCard.tokenization.domain.NolPayTokenizationInteractor
import io.primer.android.nolpay.implementation.paymentCard.tokenization.domain.model.NolPayPaymentInstrumentParams
import io.primer.android.nolpay.implementation.paymentCard.tokenization.presentation.composable.NolPayTokenizationInputable
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper
import io.primer.android.phoneMetadata.domain.PhoneMetadataInteractor
import io.primer.android.phoneMetadata.domain.model.PhoneMetadataParams

internal class NolPayTokenizationDelegate(
    private val phoneMetadataInteractor: PhoneMetadataInteractor,
    private val configurationInteractor: NolPayConfigurationInteractor,
    tokenizationInteractor: NolPayTokenizationInteractor
) : PaymentMethodTokenizationDelegate<NolPayTokenizationInputable, NolPayPaymentInstrumentParams>(
    tokenizationInteractor
),
    TokenizationCollectedDataMapper<NolPayTokenizationInputable, NolPayPaymentInstrumentParams> {

    override suspend fun mapTokenizationData(input: NolPayTokenizationInputable):
        Result<TokenizationParams<NolPayPaymentInstrumentParams>> = configurationInteractor(
        NolPayConfigParams(paymentMethodType = input.paymentMethodType)
    ).zipWith(
        phoneMetadataInteractor(PhoneMetadataParams(input.mobileNumber))
    ) { configuration, phoneMetadata ->
        TokenizationParams(
            NolPayPaymentInstrumentParams(
                paymentMethodType = input.paymentMethodType,
                paymentMethodConfigId = configuration.paymentMethodConfigId,
                locale = configuration.locale,
                mobileCountryCode = phoneMetadata.countryCode,
                mobileNumber = phoneMetadata.nationalNumber,
                nolPayCardNumber = input.nolPayCardNumber
            ),
            input.primerSessionIntent
        )
    }
}
