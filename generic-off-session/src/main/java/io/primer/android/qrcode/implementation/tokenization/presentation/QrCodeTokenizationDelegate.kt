package io.primer.android.qrcode.implementation.tokenization.presentation

import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper
import io.primer.android.qrcode.implementation.configuration.domain.QrCodeConfigurationInteractor
import io.primer.android.qrcode.implementation.configuration.domain.model.QrCodeConfigParams
import io.primer.android.qrcode.implementation.tokenization.domain.QrCodeTokenizationInteractor
import io.primer.android.qrcode.implementation.tokenization.domain.model.QrCodePaymentInstrumentParams
import io.primer.android.qrcode.implementation.tokenization.presentation.composable.QrCodeTokenizationInputable

internal class QrCodeTokenizationDelegate(
    private val configurationInteractor: QrCodeConfigurationInteractor,
    tokenizationInteractor: QrCodeTokenizationInteractor,
) : PaymentMethodTokenizationDelegate<QrCodeTokenizationInputable, QrCodePaymentInstrumentParams>(
        tokenizationInteractor,
    ),
    TokenizationCollectedDataMapper<QrCodeTokenizationInputable, QrCodePaymentInstrumentParams> {
    override suspend fun mapTokenizationData(
        input: QrCodeTokenizationInputable,
    ): Result<TokenizationParams<QrCodePaymentInstrumentParams>> =
        configurationInteractor(
            QrCodeConfigParams(paymentMethodType = input.paymentMethodType),
        ).map { configuration ->
            TokenizationParams(
                paymentInstrumentParams =
                    QrCodePaymentInstrumentParams(
                        paymentMethodType = input.paymentMethodType,
                        paymentMethodConfigId = configuration.paymentMethodConfigId,
                        locale = configuration.locale,
                    ),
                sessionIntent = input.primerSessionIntent,
            )
        }
}
