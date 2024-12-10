package io.primer.android.otp.implementation.tokenization.presentation

import io.primer.android.otp.implementation.configuration.domain.OtpConfigurationInteractor
import io.primer.android.otp.implementation.configuration.domain.model.OtpConfigParams
import io.primer.android.otp.implementation.tokenization.domain.OtpTokenizationInteractor
import io.primer.android.otp.implementation.tokenization.domain.model.OtpPaymentInstrumentParams
import io.primer.android.otp.implementation.tokenization.presentation.composable.OtpTokenizationInputable
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper

internal class OtpTokenizationDelegate(
    private val configurationInteractor: OtpConfigurationInteractor,
    tokenizationInteractor: OtpTokenizationInteractor
) : PaymentMethodTokenizationDelegate<OtpTokenizationInputable, OtpPaymentInstrumentParams>(
    tokenizationInteractor
),
    TokenizationCollectedDataMapper<OtpTokenizationInputable, OtpPaymentInstrumentParams> {

    override suspend fun mapTokenizationData(input: OtpTokenizationInputable):
        Result<TokenizationParams<OtpPaymentInstrumentParams>> = configurationInteractor(
        OtpConfigParams(paymentMethodType = input.paymentMethodType)
    ).map { configuration ->
        TokenizationParams(
            paymentInstrumentParams = OtpPaymentInstrumentParams(
                paymentMethodType = input.paymentMethodType,
                paymentMethodConfigId = configuration.paymentMethodConfigId,
                locale = configuration.locale,
                otp = input.otpData.otp
            ),
            sessionIntent = input.primerSessionIntent
        )
    }
}
