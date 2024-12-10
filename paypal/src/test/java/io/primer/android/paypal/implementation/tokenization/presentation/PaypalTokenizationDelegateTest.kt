package io.primer.android.paypal.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.paypal.implementation.tokenization.domain.PaypalConfirmBillingAgreementInteractor
import io.primer.android.paypal.implementation.tokenization.domain.PaypalOrderInfoInteractor
import io.primer.android.paypal.implementation.tokenization.domain.PaypalTokenizationInteractor
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalConfirmBillingAgreement
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfo
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalPaymentInstrumentParams
import io.primer.android.paypal.implementation.tokenization.presentation.model.PaypalTokenizationInputable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class PaypalTokenizationDelegateTest {

    private val tokenizationInteractor: PaypalTokenizationInteractor = mockk()
    private val paypalCreateOrderInteractor: PaypalOrderInfoInteractor = mockk()
    private val confirmBillingAgreementInteractor: PaypalConfirmBillingAgreementInteractor = mockk()

    private lateinit var delegate: PaypalTokenizationDelegate

    @BeforeEach
    fun setUp() {
        delegate = PaypalTokenizationDelegate(
            tokenizationInteractor,
            paypalCreateOrderInteractor,
            confirmBillingAgreementInteractor
        )
    }

    @Test
    fun `mapTokenizationData should return correct params for PaypalCheckoutTokenizationInputable`() = runTest {
        // Given
        val input = PaypalTokenizationInputable.PaypalCheckoutTokenizationInputable(
            paymentMethodConfigId = "config-id",
            paymentMethodType = "PAYPAL",
            orderId = "order-id",
            primerSessionIntent = mockk()
        )
        val orderInfo = PaypalOrderInfo(
            orderId = "order-id",
            externalPayerId = "payer-id",
            email = "email@example.com",
            externalPayerFirstName = "First",
            externalPayerLastName = "Last"
        )

        coEvery { paypalCreateOrderInteractor(any()) } returns Result.success(orderInfo)

        // When
        val result = delegate.mapTokenizationData(input)

        // Then
        coVerify { paypalCreateOrderInteractor(any()) }
        assert(result.isSuccess)
        val tokenizationParams = result.getOrThrow()
        assertEquals(input.primerSessionIntent, tokenizationParams.sessionIntent)
        val paymentInstrumentParams = tokenizationParams.paymentInstrumentParams
            as PaypalPaymentInstrumentParams.PaypalCheckoutPaymentInstrumentParams
        assertEquals("PAYPAL", paymentInstrumentParams.paymentMethodType)
        assertEquals("payer-id", paymentInstrumentParams.externalPayerId)
        assertEquals("email@example.com", paymentInstrumentParams.externalPayerInfoEmail)
        assertEquals("order-id", paymentInstrumentParams.paypalOrderId)
        assertEquals("First", paymentInstrumentParams.externalPayerFirstName)
        assertEquals("Last", paymentInstrumentParams.externalPayerLastName)
    }

    @Test
    fun `mapTokenizationData should return correct params for PaypalVaultTokenizationInputable`() = runTest {
        // Given
        val input = PaypalTokenizationInputable.PaypalVaultTokenizationInputable(
            paymentMethodConfigId = "config-id",
            paymentMethodType = "PAYPAL",
            tokenId = "token-id",
            primerSessionIntent = mockk()
        )
        val billingAgreement = PaypalConfirmBillingAgreement(
            billingAgreementId = "agreement-id",
            externalPayerInfo = mockk(),
            shippingAddress = mockk()
        )

        coEvery { confirmBillingAgreementInteractor(any()) } returns Result.success(billingAgreement)

        // When
        val result = delegate.mapTokenizationData(input)

        // Then
        coVerify { confirmBillingAgreementInteractor(any()) }
        assert(result.isSuccess)
        val tokenizationParams = result.getOrThrow()
        assertEquals(input.primerSessionIntent, tokenizationParams.sessionIntent)
        val paymentInstrumentParams = tokenizationParams.paymentInstrumentParams
            as PaypalPaymentInstrumentParams.PaypalVaultPaymentInstrumentParams
        assertEquals("agreement-id", paymentInstrumentParams.paypalBillingAgreementId)
        assertEquals("PAYPAL", paymentInstrumentParams.paymentMethodType)
        assertEquals(billingAgreement.externalPayerInfo, paymentInstrumentParams.externalPayerInfo)
        assertEquals(billingAgreement.shippingAddress, paymentInstrumentParams.shippingAddress)
    }
}
