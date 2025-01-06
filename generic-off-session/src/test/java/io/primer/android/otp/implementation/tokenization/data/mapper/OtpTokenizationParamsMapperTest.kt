package io.primer.android.otp.implementation.tokenization.data.mapper

import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.otp.implementation.tokenization.data.model.AdyenBlikSessionInfoDataRequest
import io.primer.android.otp.implementation.tokenization.data.model.OtpPaymentInstrumentDataRequest
import io.primer.android.otp.implementation.tokenization.domain.model.OtpPaymentInstrumentParams
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OtpTokenizationParamsMapperTest {
    private val mapper = OtpTokenizationParamsMapper()

    @Test
    fun `map() correctly maps OtpPaymentInstrumentParams to OtpPaymentInstrumentDataRequest when payment method type is ADYEN_BLIK`() {
        val params =
            TokenizationParams(
                OtpPaymentInstrumentParams(
                    paymentMethodType = PaymentMethodType.ADYEN_BLIK.name,
                    paymentMethodConfigId = "id",
                    locale = "en_US",
                    otp = "1234",
                ),
                sessionIntent = PrimerSessionIntent.CHECKOUT,
            )

        val result = mapper.map(params)

        val expectedInstrumentDataRequest =
            OtpPaymentInstrumentDataRequest(
                paymentMethodType = PaymentMethodType.ADYEN_BLIK.name,
                paymentMethodConfigId = "id",
                sessionInfo = AdyenBlikSessionInfoDataRequest(locale = "en_US", blikCode = "1234"),
                type = PaymentInstrumentType.OFF_SESSION_PAYMENT,
            )
        val expectedTokenizationRequest =
            expectedInstrumentDataRequest.toTokenizationRequest(PrimerSessionIntent.CHECKOUT)

        assertEquals(expectedTokenizationRequest.paymentInstrument, result.paymentInstrument)
    }

    @Test
    fun `map() throws exception when payment method type is not ADYEN_BLIK`() {
        val params =
            TokenizationParams(
                OtpPaymentInstrumentParams(
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                    paymentMethodConfigId = "id",
                    locale = "en_US",
                    otp = "1234",
                ),
                sessionIntent = PrimerSessionIntent.CHECKOUT,
            )

        assertThrows<IllegalStateException> {
            mapper.map(params)
        }
    }
}
