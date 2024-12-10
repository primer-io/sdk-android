package io.primer.android.phoneNumber.implementation.tokenization.presentation

import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper
import io.primer.android.phoneNumber.implementation.configuration.domain.PhoneNumberConfigurationInteractor
import io.primer.android.phoneNumber.implementation.configuration.domain.model.PhoneNumberConfigParams
import io.primer.android.phoneNumber.implementation.tokenization.domain.PhoneNumberTokenizationInteractor
import io.primer.android.phoneNumber.implementation.tokenization.domain.model.PhoneNumberPaymentInstrumentParams
import io.primer.android.phoneNumber.implementation.tokenization.presentation.composable.PhoneNumberTokenizationInputable

internal class PhoneNumberTokenizationDelegate(
    private val configurationInteractor: PhoneNumberConfigurationInteractor,
    tokenizationInteractor: PhoneNumberTokenizationInteractor
) : PaymentMethodTokenizationDelegate<PhoneNumberTokenizationInputable, PhoneNumberPaymentInstrumentParams>(
    tokenizationInteractor
),
    TokenizationCollectedDataMapper<PhoneNumberTokenizationInputable, PhoneNumberPaymentInstrumentParams> {

    override suspend fun mapTokenizationData(input: PhoneNumberTokenizationInputable):
        Result<TokenizationParams<PhoneNumberPaymentInstrumentParams>> = configurationInteractor(
        PhoneNumberConfigParams(paymentMethodType = input.paymentMethodType)
    ).map { configuration ->
        TokenizationParams(
            paymentInstrumentParams = PhoneNumberPaymentInstrumentParams(
                paymentMethodType = input.paymentMethodType,
                paymentMethodConfigId = configuration.paymentMethodConfigId,
                locale = configuration.locale,
                phoneNumber = input.phoneNumberData.phoneNumber
            ),
            sessionIntent = input.primerSessionIntent
        )
    }
}
