package io.primer.android.payments.core.tokenization.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.data.tokenization.models.TokenType
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class TokenizationInteractorTest {

    @Test
    fun `performAction should call preTokenizationHandler and tokenizationRepository with the expected args`() {
        // Mock dependencies
        val tokenizationRepository = mockk<TokenizationRepository<BasePaymentInstrumentParams>>()
        val tokenizedPaymentMethodRepository = mockk<TokenizedPaymentMethodRepository>()
        val preTokenizationHandler = mockk<PreTokenizationHandler>()
        val logReporter = mockk<LogReporter>()

        // Create instance of TokenizationInteractor
        val interactor = object : TokenizationInteractor<BasePaymentInstrumentParams>(
            tokenizationRepository,
            tokenizedPaymentMethodRepository,
            preTokenizationHandler,
            logReporter,
            Dispatchers.Unconfined // Use Unconfined dispatcher for testing
        ) {}

        // Prepare test data
        val paymentMethodToken = PaymentMethodTokenInternal(
            "mockToken",
            "mockPaymentInstrumentType",
            "mockPaymentMethodType",
            null,
            null,
            null,
            true,
            "mockAnalyticsId",
            TokenType.SINGLE_USE
        )
        val params = TokenizationParams(
            mockk<BasePaymentInstrumentParams> {
                every { paymentMethodType } returns "mockPaymentMethodType"
            },
            PrimerSessionIntent.CHECKOUT
        )

        // Mock preTokenizationHandler response
        coEvery { preTokenizationHandler.handle(any(), any()) } returns Result.success(Unit)

        // Mock tokenizationRepository behavior
        coEvery { tokenizationRepository.tokenize(params) } returns Result.success(paymentMethodToken)

        // Mock tokenizedPaymentMethodRepository behavior
        every { tokenizedPaymentMethodRepository.setPaymentMethod(any()) } just runs

        // Mock logReporter behavior
        every { logReporter.info(any()) } just runs

        // Perform the action
        val result = runBlocking { interactor(params) }

        // Verify interactions and assertions
        coVerify { preTokenizationHandler.handle(any(), any()) }
        coVerify { tokenizationRepository.tokenize(params) }
        verify { tokenizedPaymentMethodRepository.setPaymentMethod(paymentMethodToken) }
        verify { logReporter.info("Started tokenization for mockPaymentMethodType payment method.") }
        verify { logReporter.info("Tokenization successful for mockPaymentMethodType payment method.") }

        // Assert the result
        assertEquals(Result.success(paymentMethodToken), result)
    }
}
