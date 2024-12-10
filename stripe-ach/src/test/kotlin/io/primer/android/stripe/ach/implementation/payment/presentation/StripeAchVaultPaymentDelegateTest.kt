package io.primer.android.stripe.ach.implementation.payment.presentation

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.core.extensions.toIso8601String
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.repository.PaymentResultRepository
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.ManualFlowSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PendingResumeHandler
import io.primer.android.stripe.ach.api.additionalInfo.AchAdditionalInfo
import io.primer.android.stripe.ach.implementation.payment.confirmation.presentation.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.stripe.ach.implementation.payment.resume.handler.StripeAchVaultDecision
import io.primer.android.stripe.ach.implementation.payment.resume.handler.StripeAchVaultResumeDecisionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date
import kotlin.time.Duration.Companion.seconds

@ExtendWith(MockKExtension::class)
class StripeAchVaultPaymentDelegateTest {
    private lateinit var stripeAchVaultPaymentDelegate: StripeAchVaultPaymentDelegate

    @MockK
    private lateinit var pendingResumeHandler: PendingResumeHandler

    @MockK
    private lateinit var manualFlowSuccessHandler: ManualFlowSuccessHandler

    @MockK
    private lateinit var successHandler: CheckoutSuccessHandler

    @MockK
    private lateinit var errorHandler: CheckoutErrorHandler

    @MockK
    private lateinit var baseErrorResolver: BaseErrorResolver

    @MockK
    private lateinit var paymentResultRepository: PaymentResultRepository

    @MockK
    private lateinit var resumeDecisionHandler: StripeAchVaultResumeDecisionHandler

    @MockK
    private lateinit var completeStripeAchPaymentSessionDelegate: CompleteStripeAchPaymentSessionDelegate

    @MockK
    private lateinit var config: PrimerConfig

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("io.primer.android.core.extensions.DateKt")
        stripeAchVaultPaymentDelegate = spyk(
            StripeAchVaultPaymentDelegate(
                paymentMethodTokenHandler = mockk(),
                resumePaymentHandler = mockk(),
                successHandler = successHandler,
                errorHandler = errorHandler,
                baseErrorResolver = baseErrorResolver,
                resumeDecisionHandler = resumeDecisionHandler,
                completeStripeAchPaymentSessionDelegate = completeStripeAchPaymentSessionDelegate,
                pendingResumeHandler = pendingResumeHandler,
                manualFlowSuccessHandler = manualFlowSuccessHandler,
                paymentResultRepository = paymentResultRepository,
                config = config
            )
        )
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(
            successHandler,
            errorHandler,
            baseErrorResolver,
            resumeDecisionHandler,
            completeStripeAchPaymentSessionDelegate,
            pendingResumeHandler,
            manualFlowSuccessHandler,
            paymentResultRepository,
            config
        )
        unmockkStatic("io.primer.android.core.extensions.DateKt")
    }

    @Test
    fun `given manual mode relevant handlers and delegates are called`() = runBlocking {
        // Arrange
        val stripeAchVaultDecision = mockk<StripeAchVaultDecision> {
            every { sdkCompleteUrl } returns "sdkCompleteUrl"
        }
        coEvery { resumeDecisionHandler.continueWithNewClientToken("clientToken") } returns Result.success(
            stripeAchVaultDecision
        )
        every { config.settings.paymentHandling } returns PrimerPaymentHandling.MANUAL
        coEvery { completeStripeAchPaymentSessionDelegate.invoke(any(), any(), any()) } returns Result.success(Unit)
        every { any<Date>().toIso8601String() } returns "iso8601"
        coEvery { pendingResumeHandler.handle(any()) } just Runs
        coEvery { manualFlowSuccessHandler.handle() } just Runs

        // Act
        launch {
            stripeAchVaultPaymentDelegate.handleNewClientToken("clientToken", mockk())
        }
        delay(1.seconds)

        // Assert
        coVerify {
            resumeDecisionHandler.continueWithNewClientToken("clientToken")
            completeStripeAchPaymentSessionDelegate.invoke("sdkCompleteUrl", null, any<Date>())
            manualFlowSuccessHandler.handle()
        }
        verify {
            config.settings.paymentHandling
            pendingResumeHandler.handle(AchAdditionalInfo.MandateAccepted("iso8601"))
        }
    }

    @Test
    fun `given auto mode relevant handlers and delegates are called`() = runBlocking {
        // Arrange
        val stripeAchVaultDecision = mockk<StripeAchVaultDecision> {
            every { sdkCompleteUrl } returns "sdkCompleteUrl"
        }
        coEvery { resumeDecisionHandler.continueWithNewClientToken("clientToken") } returns Result.success(
            stripeAchVaultDecision
        )
        every { config.settings.paymentHandling } returns PrimerPaymentHandling.AUTO
        coEvery { completeStripeAchPaymentSessionDelegate.invoke(any(), any(), any()) } returns Result.success(Unit)
        coEvery { successHandler.handle(any(), any()) } just Runs
        val payment = mockk<Payment>()
        every { paymentResultRepository.getPaymentResult().payment } returns payment

        // Act
        launch {
            stripeAchVaultPaymentDelegate.handleNewClientToken("clientToken", mockk())
        }
        delay(1.seconds)

        // Assert
        coVerify {
            resumeDecisionHandler.continueWithNewClientToken("clientToken")
            completeStripeAchPaymentSessionDelegate.invoke("sdkCompleteUrl", null, any<Date>())
            successHandler.handle(payment = payment, additionalInfo = null)
        }
        verify {
            config.settings.paymentHandling
            paymentResultRepository.getPaymentResult().payment
        }
    }

    @Test
    fun `when continueWithNewClientToken fails then error is handled`() = runBlocking {
        // Arrange
        val exception = Exception("error")
        coEvery { resumeDecisionHandler.continueWithNewClientToken(any()) } returns Result.failure(exception)
        val error = mockk<PrimerError>()
        every { baseErrorResolver.resolve(any()) } returns error
        coEvery { errorHandler.handle(any(), any()) } just Runs

        // Act
        val result = stripeAchVaultPaymentDelegate.handleNewClientToken("clientToken", mockk())

        // Assert
        assertTrue(result.isFailure)
        coVerify {
            resumeDecisionHandler.continueWithNewClientToken("clientToken")
            errorHandler.handle(error = error, payment = null)
        }
        verify {
            baseErrorResolver.resolve(exception)
        }
        coVerify(exactly = 0) { completeStripeAchPaymentSessionDelegate.invoke(any(), any(), any()) }
    }

    @Test
    fun `given manual mode, when pending resume handler fails then error is handled`() = runBlocking {
        // Arrange
        val stripeAchVaultDecision = mockk<StripeAchVaultDecision> {
            every { sdkCompleteUrl } returns "sdkCompleteUrl"
        }
        coEvery { resumeDecisionHandler.continueWithNewClientToken("clientToken") } returns Result.success(
            stripeAchVaultDecision
        )
        every { config.settings.paymentHandling } returns PrimerPaymentHandling.MANUAL
        coEvery { completeStripeAchPaymentSessionDelegate.invoke(any(), any(), any()) } returns Result.success(Unit)
        every { any<Date>().toIso8601String() } returns "iso8601"
        val exception = Exception("error")
        coEvery { pendingResumeHandler.handle(any()) } throws exception
        val error = mockk<PrimerError>()
        every { baseErrorResolver.resolve(any()) } returns error
        coEvery { errorHandler.handle(any(), any()) } just Runs

        // Act
        launch {
            stripeAchVaultPaymentDelegate.handleNewClientToken("clientToken", mockk())
        }
        delay(1.seconds)

        // Assert
        coVerify {
            resumeDecisionHandler.continueWithNewClientToken("clientToken")
            completeStripeAchPaymentSessionDelegate.invoke("sdkCompleteUrl", null, any<Date>())
        }
        verify {
            config.settings.paymentHandling
            pendingResumeHandler.handle(AchAdditionalInfo.MandateAccepted("iso8601"))
        }
        coVerify {
            resumeDecisionHandler.continueWithNewClientToken("clientToken")
            errorHandler.handle(error = error, payment = null)
        }
        verify {
            baseErrorResolver.resolve(exception)
        }
    }

    @Test
    fun `given manual mode, when manual flow success handler fails then error is handled`() = runBlocking {
        // Arrange
        val stripeAchVaultDecision = mockk<StripeAchVaultDecision> {
            every { sdkCompleteUrl } returns "sdkCompleteUrl"
        }
        coEvery { resumeDecisionHandler.continueWithNewClientToken("clientToken") } returns Result.success(
            stripeAchVaultDecision
        )
        every { config.settings.paymentHandling } returns PrimerPaymentHandling.MANUAL
        coEvery { completeStripeAchPaymentSessionDelegate.invoke(any(), any(), any()) } returns Result.success(Unit)
        every { any<Date>().toIso8601String() } returns "iso8601"
        val exception = Exception("error")
        coEvery { pendingResumeHandler.handle(any()) } just Runs
        coEvery { manualFlowSuccessHandler.handle() } throws exception
        val error = mockk<PrimerError>()
        every { baseErrorResolver.resolve(any()) } returns error
        coEvery { errorHandler.handle(any(), any()) } just Runs

        // Act
        launch {
            stripeAchVaultPaymentDelegate.handleNewClientToken("clientToken", mockk())
        }
        delay(1.seconds)

        // Assert
        coVerify {
            resumeDecisionHandler.continueWithNewClientToken("clientToken")
            completeStripeAchPaymentSessionDelegate.invoke("sdkCompleteUrl", null, any<Date>())
            manualFlowSuccessHandler.handle()
        }
        coVerify {
            resumeDecisionHandler.continueWithNewClientToken("clientToken")
            errorHandler.handle(error = error, payment = null)
        }
        verify {
            config.settings.paymentHandling
            pendingResumeHandler.handle(AchAdditionalInfo.MandateAccepted("iso8601"))
            baseErrorResolver.resolve(exception)
        }
    }

    @Test
    fun `given auto mode, when success handler fails then error is handled`() = runBlocking {
        // Arrange
        val stripeAchVaultDecision = mockk<StripeAchVaultDecision> {
            every { sdkCompleteUrl } returns "sdkCompleteUrl"
        }
        coEvery { resumeDecisionHandler.continueWithNewClientToken("clientToken") } returns Result.success(
            stripeAchVaultDecision
        )
        every { config.settings.paymentHandling } returns PrimerPaymentHandling.AUTO
        coEvery { completeStripeAchPaymentSessionDelegate.invoke(any(), any(), any()) } returns Result.success(Unit)
        val exception = Exception("error")
        coEvery { successHandler.handle(any(), any()) } throws exception
        val payment = mockk<Payment>()
        every { paymentResultRepository.getPaymentResult().payment } returns payment
        val error = mockk<PrimerError>()
        every { baseErrorResolver.resolve(any()) } returns error
        coEvery { errorHandler.handle(any(), any()) } just Runs

        // Act
        launch {
            stripeAchVaultPaymentDelegate.handleNewClientToken("clientToken", mockk())
        }
        delay(1.seconds)

        // Assert
        coVerify {
            resumeDecisionHandler.continueWithNewClientToken("clientToken")
            completeStripeAchPaymentSessionDelegate.invoke("sdkCompleteUrl", null, any<Date>())
            successHandler.handle(payment = payment, additionalInfo = null)
            errorHandler.handle(error = error, payment = null)
        }
        verify {
            config.settings.paymentHandling
            paymentResultRepository.getPaymentResult().payment
            baseErrorResolver.resolve(exception)
        }
    }

    @Test
    fun `given auto mode, when payment result repository fails then error is handled`() = runBlocking {
        // Arrange
        val stripeAchVaultDecision = mockk<StripeAchVaultDecision> {
            every { sdkCompleteUrl } returns "sdkCompleteUrl"
        }
        coEvery { resumeDecisionHandler.continueWithNewClientToken("clientToken") } returns Result.success(
            stripeAchVaultDecision
        )
        every { config.settings.paymentHandling } returns PrimerPaymentHandling.AUTO
        coEvery { completeStripeAchPaymentSessionDelegate.invoke(any(), any(), any()) } returns Result.success(Unit)
        val exception = Exception("error")
        val payment = mockk<Payment>()
        every { paymentResultRepository.getPaymentResult().payment } throws exception
        val error = mockk<PrimerError>()
        every { baseErrorResolver.resolve(any()) } returns error
        coEvery { errorHandler.handle(any(), any()) } just Runs

        // Act
        launch {
            stripeAchVaultPaymentDelegate.handleNewClientToken("clientToken", mockk())
        }
        delay(1.seconds)

        // Assert
        coVerify {
            resumeDecisionHandler.continueWithNewClientToken("clientToken")
            completeStripeAchPaymentSessionDelegate.invoke("sdkCompleteUrl", null, any<Date>())
            errorHandler.handle(error = error, payment = null)
        }
        verify {
            config.settings.paymentHandling
            paymentResultRepository.getPaymentResult().payment
            baseErrorResolver.resolve(exception)
        }
        coVerify(exactly = 0) {
            successHandler.handle(payment = any(), additionalInfo = any())
        }
    }
}
