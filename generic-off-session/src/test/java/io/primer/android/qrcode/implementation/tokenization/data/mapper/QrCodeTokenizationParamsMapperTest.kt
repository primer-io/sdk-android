package io.primer.android.qrcode.implementation.tokenization.data.mapper

import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.qrcode.implementation.tokenization.data.model.QrCodePaymentInstrumentDataRequest
import io.primer.android.qrcode.implementation.tokenization.data.model.QrCodeSessionInfoDataRequest
import io.primer.android.qrcode.implementation.tokenization.domain.model.QrCodePaymentInstrumentParams
import org.junit.jupiter.api.Test

internal class QrCodeTokenizationParamsMapperTest {

    private val mapper = QrCodeTokenizationParamsMapper()

    @Test
    fun `map should correctly map TokenizationParams to TokenizationRequestV2`() {
        // Given
        val paymentMethodType = "qrCode"
        val paymentInstrumentParams = QrCodePaymentInstrumentParams(
            paymentMethodType = paymentMethodType,
            paymentMethodConfigId = "testPaymentMethodConfigId",
            locale = "testLocale"
        )
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val tokenizationParams = TokenizationParams(paymentInstrumentParams, sessionIntent)

        // When
        val result = mapper.map(tokenizationParams)

        val instrumentRequest = QrCodePaymentInstrumentDataRequest(
            paymentMethodType = paymentMethodType,
            paymentMethodConfigId = "testPaymentMethodConfigId",
            sessionInfo = QrCodeSessionInfoDataRequest(
                locale = "testLocale"
            ),
            type = PaymentInstrumentType.OFF_SESSION_PAYMENT
        )

        // Then
        val expectedRequest = instrumentRequest.toTokenizationRequest(sessionIntent)

        assert(result == expectedRequest)
    }
}
