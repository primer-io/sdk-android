package io.primer.android.stripe.ach.implementation.tokenization.domain

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.stripe.ach.implementation.tokenization.domain.model.StripeAchPaymentInstrumentParams
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class StripeAchTokenizationInteractorTest {
    @MockK
    private lateinit var tokenizationRepository: TokenizationRepository<StripeAchPaymentInstrumentParams>

    @MockK
    private lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository

    @MockK
    private lateinit var preTokenizationHandler: PreTokenizationHandler

    @MockK
    private lateinit var logReporter: LogReporter

    @InjectMockKs
    private lateinit var interactor: StripeAchTokenizationInteractor

    @Test
    fun `invoke() should return successful result when tokenization is successful`() =
        runTest {
            val params =
                mockk<TokenizationParams<StripeAchPaymentInstrumentParams>> {
                    every { paymentInstrumentParams.paymentMethodType } returns PaymentMethodType.STRIPE_ACH.name
                    every { sessionIntent } returns PrimerSessionIntent.CHECKOUT
                }
            val tokenizedPaymentMethod = mockk<PaymentMethodTokenInternal>()

            coEvery { preTokenizationHandler.handle(any(), any()) } returns Result.success(Unit)
            coEvery { tokenizationRepository.tokenize(params) } returns Result.success(tokenizedPaymentMethod)
            every { logReporter.info(any()) } just Runs
            every { tokenizedPaymentMethodRepository.setPaymentMethod(tokenizedPaymentMethod) } just Runs

            val result = interactor.invoke(params)

            assertTrue(result.isSuccess)
            assertEquals(tokenizedPaymentMethod, result.getOrNull())
            coVerify {
                preTokenizationHandler.handle(
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                    sessionIntent = PrimerSessionIntent.CHECKOUT,
                )
                tokenizationRepository.tokenize(params)
            }
            verify {
                logReporter.info("Started tokenization for STRIPE_ACH payment method.")
                logReporter.info("Tokenization successful for STRIPE_ACH payment method.")
                tokenizedPaymentMethodRepository.setPaymentMethod(tokenizedPaymentMethod)
            }
        }

    @Test
    fun `invoke() should return failure result when tokenization is preTokenizationHandler fails`() =
        runTest {
            val params =
                mockk<TokenizationParams<StripeAchPaymentInstrumentParams>> {
                    every { paymentInstrumentParams.paymentMethodType } returns PaymentMethodType.STRIPE_ACH.name
                    every { sessionIntent } returns PrimerSessionIntent.CHECKOUT
                }
            val error = Exception()
            coEvery { preTokenizationHandler.handle(any(), any()) } returns Result.failure(error)

            val result = interactor.invoke(params)

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
            coVerify {
                preTokenizationHandler.handle(
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                    sessionIntent = PrimerSessionIntent.CHECKOUT,
                )
            }
            coVerify(exactly = 0) {
                tokenizationRepository.tokenize(any())
            }
            verify(exactly = 0) {
                logReporter.info(any())
                tokenizedPaymentMethodRepository.setPaymentMethod(any())
            }
        }

    @Test
    fun `invoke() should return failure result when tokenization is tokenizationRepository fails`() =
        runTest {
            val params =
                mockk<TokenizationParams<StripeAchPaymentInstrumentParams>> {
                    every { paymentInstrumentParams.paymentMethodType } returns PaymentMethodType.STRIPE_ACH.name
                    every { sessionIntent } returns PrimerSessionIntent.CHECKOUT
                }
            val error = Exception()
            coEvery { preTokenizationHandler.handle(any(), any()) } returns Result.success(Unit)
            coEvery { tokenizationRepository.tokenize(params) } returns Result.failure(error)
            every { logReporter.info(any()) } just Runs

            val result = interactor.invoke(params)

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
            coVerify {
                preTokenizationHandler.handle(
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                    sessionIntent = PrimerSessionIntent.CHECKOUT,
                )
                tokenizationRepository.tokenize(any())
            }
            verify {
                logReporter.info("Started tokenization for STRIPE_ACH payment method.")
            }
            verify(exactly = 0) {
                logReporter.info("Tokenization successful for STRIPE_ACH payment method.")
                tokenizedPaymentMethodRepository.setPaymentMethod(any())
            }
        }
}
