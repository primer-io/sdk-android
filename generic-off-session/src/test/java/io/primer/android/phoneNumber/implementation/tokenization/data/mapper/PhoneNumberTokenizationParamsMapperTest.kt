package io.primer.android.phoneNumber.implementation.tokenization.data.mapper

import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.phoneNumber.implementation.tokenization.data.model.PhoneNumberPaymentInstrumentDataRequest
import io.primer.android.phoneNumber.implementation.tokenization.data.model.PhoneNumberSessionInfoDataRequest
import io.primer.android.phoneNumber.implementation.tokenization.domain.model.PhoneNumberPaymentInstrumentParams
import org.junit.jupiter.api.Test

internal class PhoneNumberTokenizationParamsMapperTest {
    private val mapper = PhoneNumberTokenizationParamsMapper()

    @Test
    fun `map should correctly map TokenizationParams to TokenizationRequestV2`() {
        // Given
        val paymentMethodType = "phoneNumber"
        val paymentInstrumentParams =
            PhoneNumberPaymentInstrumentParams(
                paymentMethodType = paymentMethodType,
                paymentMethodConfigId = "testPaymentMethodConfigId",
                locale = "testLocale",
                phoneNumber = "testPhoneNumber",
            )
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val tokenizationParams = TokenizationParams(paymentInstrumentParams, sessionIntent)

        // When
        val result = mapper.map(tokenizationParams)

        val instrumentRequest =
            PhoneNumberPaymentInstrumentDataRequest(
                paymentMethodType = paymentMethodType,
                paymentMethodConfigId = "testPaymentMethodConfigId",
                sessionInfo =
                    PhoneNumberSessionInfoDataRequest(
                        locale = "testLocale",
                        phoneNumber = "testPhoneNumber",
                    ),
                type = PaymentInstrumentType.OFF_SESSION_PAYMENT,
            )

        // Then
        val expectedRequest = instrumentRequest.toTokenizationRequest(sessionIntent)

        assert(result == expectedRequest)
    }
}
