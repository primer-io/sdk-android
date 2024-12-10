package io.primer.android.paypal.implementation.tokenization.data.mapper

import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.data.model.TokenizationCheckoutRequestV2
import io.primer.android.payments.core.tokenization.data.model.TokenizationVaultRequestV2
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.paypal.implementation.tokenization.data.model.ExternalPayerInfoRequest
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalExternalPayerInfo
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalPaymentInstrumentDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalShippingAddressDataResponse
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalPaymentInstrumentParams
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PaypalTokenizationParamsMapperTest {

    private lateinit var mapper: PaypalTokenizationParamsMapper

    @BeforeEach
    fun setUp() {
        mapper = PaypalTokenizationParamsMapper()
    }

    @Test
    fun `map should return TokenizationRequestV2 for PaypalCheckoutPaymentInstrumentParams`() {
        // Arrange
        val paypalOrderId = "order123"
        val email = "email@example.com"
        val externalPayerId = "payer123"
        val firstName = "John"
        val lastName = "Doe"
        val paymentInstrumentParams = PaypalPaymentInstrumentParams.PaypalCheckoutPaymentInstrumentParams(
            paypalOrderId = paypalOrderId,
            externalPayerInfoEmail = email,
            externalPayerId = externalPayerId,
            externalPayerFirstName = firstName,
            externalPayerLastName = lastName
        )
        val tokenizationParams = TokenizationParams<PaypalPaymentInstrumentParams>(
            paymentInstrumentParams = paymentInstrumentParams,
            sessionIntent = PrimerSessionIntent.CHECKOUT
        )

        // Act
        val result = mapper.map(tokenizationParams)

        // Assert
        val expectedRequest = PaypalPaymentInstrumentDataRequest.PaypalCheckoutPaymentInstrumentDataRequest(
            paypalOrderId = paypalOrderId,
            externalPayerInfo = ExternalPayerInfoRequest(
                email = email,
                externalPayerId = externalPayerId,
                firstName = firstName,
                lastName = lastName
            )
        )
        val expectedTokenizationRequest = TokenizationCheckoutRequestV2<PaypalPaymentInstrumentDataRequest>(
            paymentInstrument = expectedRequest,
            paymentInstrumentSerializer = PaypalPaymentInstrumentDataRequest.serializer
        )
        assertEquals(expectedTokenizationRequest, result)
    }

    @Test
    fun `map should return TokenizationRequestV2 for PaypalVaultPaymentInstrumentParams`() {
        // Arrange
        val billingAgreementId = "agreement123"
        val externalPayerInfo = mockk<PaypalExternalPayerInfo>()
        val shippingAddress = mockk<PaypalShippingAddressDataResponse>()
        val paymentInstrumentParams = PaypalPaymentInstrumentParams.PaypalVaultPaymentInstrumentParams(
            paypalBillingAgreementId = billingAgreementId,
            externalPayerInfo = externalPayerInfo,
            shippingAddress = shippingAddress
        )
        val tokenizationParams = TokenizationParams<PaypalPaymentInstrumentParams>(
            paymentInstrumentParams = paymentInstrumentParams,
            sessionIntent = PrimerSessionIntent.VAULT
        )

        // Act
        val result = mapper.map(tokenizationParams)

        // Assert
        val expectedRequest = PaypalPaymentInstrumentDataRequest.PaypalVaultPaymentInstrumentDataRequest(
            billingAgreementId = billingAgreementId,
            externalPayerInfo = externalPayerInfo,
            shippingAddress = shippingAddress
        )
        val expectedTokenizationRequest = TokenizationVaultRequestV2<PaypalPaymentInstrumentDataRequest>(
            paymentInstrument = expectedRequest,
            paymentInstrumentSerializer = PaypalPaymentInstrumentDataRequest.serializer,
            tokenType = "MULTI_USE",
            paymentFlow = "VAULT"
        )
        assertEquals(expectedTokenizationRequest, result)
    }
}
