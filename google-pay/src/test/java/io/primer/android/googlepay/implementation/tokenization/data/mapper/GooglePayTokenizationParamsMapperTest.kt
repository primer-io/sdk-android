package io.primer.android.googlepay.implementation.tokenization.data.mapper

import com.google.android.gms.wallet.PaymentData
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.primer.android.PrimerSessionIntent
import io.primer.android.googlepay.implementation.tokenization.data.mapper.GooglePayTokenizationParamsMapper.Companion.PAYMENT_METHOD_DATA_FIELD
import io.primer.android.googlepay.implementation.tokenization.data.mapper.GooglePayTokenizationParamsMapper.Companion.TOKENIZATION_DATA_FIELD
import io.primer.android.googlepay.implementation.tokenization.data.mapper.GooglePayTokenizationParamsMapper.Companion.TOKENIZATION_FIELD
import io.primer.android.googlepay.implementation.tokenization.data.model.GooglePayPaymentInstrumentDataRequest
import io.primer.android.googlepay.implementation.tokenization.domain.model.GooglePayFlow
import io.primer.android.googlepay.implementation.tokenization.domain.model.GooglePayPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import org.json.JSONObject
import org.junit.jupiter.api.Test
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class GooglePayTokenizationParamsMapperTest {
    private val mapper = GooglePayTokenizationParamsMapper()

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `map should correctly map TokenizationParams to TokenizationRequestV2`() {
        // Given
        val merchantId = "testMerchantId"
        val paymentData = mockk<PaymentData>()
        val token = "testToken"
        val encodedToken = Base64.encode(token.toByteArray())

        mockkStatic(android.util.Base64::class)
        every { android.util.Base64.encodeToString(any(), any()) } returns encodedToken

        val paymentDataJson =
            JSONObject().apply {
                put(
                    PAYMENT_METHOD_DATA_FIELD,
                    JSONObject().apply {
                        put(
                            TOKENIZATION_DATA_FIELD,
                            JSONObject().apply {
                                put(
                                    TOKENIZATION_FIELD,
                                    token,
                                )
                            },
                        )
                    },
                )
            }.toString()
        every { paymentData.toJson() } returns paymentDataJson
        val flow = GooglePayFlow.GATEWAY
        val paymentInstrumentParams =
            GooglePayPaymentInstrumentParams(
                paymentMethodType = "google_pay",
                merchantId = merchantId,
                paymentData = paymentData,
                flow = flow,
            )
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val tokenizationParams = TokenizationParams(paymentInstrumentParams, sessionIntent)

        // When
        val result = mapper.map(tokenizationParams)

        val instrumentRequest =
            GooglePayPaymentInstrumentDataRequest(
                merchantId = merchantId,
                encryptedPayload = encodedToken,
                flow = flow,
            )

        // Then
        val expectedRequest = instrumentRequest.toTokenizationRequest(sessionIntent)
        assert(result == expectedRequest)
    }
}
