package io.primer.android.nolpay.implementation.paymentCard.tokenization.data.mapper

import android.os.Build
import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.nolpay.implementation.paymentCard.tokenization.data.model.NolPayPaymentInstrumentDataRequest
import io.primer.android.nolpay.implementation.paymentCard.tokenization.data.model.NolPaySessionInfoDataRequest
import io.primer.android.nolpay.implementation.paymentCard.tokenization.domain.model.NolPayPaymentInstrumentParams
import io.primer.android.nolpay.modifyClassProperty
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class NolPayTokenizationParamsMapperTest {

    private lateinit var mapper: NolPayTokenizationParamsMapper

    @BeforeEach
    fun setUp() {
        mapper = NolPayTokenizationParamsMapper()
    }

    @Test
    fun `map should correctly transform NolPayPaymentInstrumentParams to NolPayPaymentInstrumentDataRequest`() {
        val manufacturer = "Samsung"
        val model = "S20"

        modifyClassProperty<Build>("MANUFACTURER", manufacturer)
        modifyClassProperty<Build>("MODEL", model)

        val paymentInstrumentParams = NolPayPaymentInstrumentParams(
            paymentMethodType = "NOL_PAY",
            paymentMethodConfigId = "configId",
            mobileCountryCode = "US",
            mobileNumber = "1234567890",
            nolPayCardNumber = "1234567812345678",
            locale = "en"
        )

        val tokenizationParams = TokenizationParams(
            paymentInstrumentParams = paymentInstrumentParams,
            sessionIntent = PrimerSessionIntent.CHECKOUT
        )

        val expectedRequest = NolPayPaymentInstrumentDataRequest(
            paymentMethodType = "NOL_PAY",
            paymentMethodConfigId = "configId",
            sessionInfo = NolPaySessionInfoDataRequest(
                mobileCountryCode = "US",
                mobileNumber = "1234567890",
                nolPayCardNumber = "1234567812345678",
                deviceVendor = manufacturer,
                deviceModel = model
            ),
            type = PaymentInstrumentType.OFF_SESSION_PAYMENT
        ).toTokenizationRequest(PrimerSessionIntent.CHECKOUT)

        val result = mapper.map(tokenizationParams)

        assertEquals(expectedRequest, result)
    }
}
