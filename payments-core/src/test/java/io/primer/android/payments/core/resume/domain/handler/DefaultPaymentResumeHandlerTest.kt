package io.primer.android.payments.core.resume.domain.handler

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.resume.domain.ResumePaymentInteractor
import io.primer.android.payments.core.resume.domain.models.ResumeParams
import io.primer.android.payments.di.PaymentsContainer
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DefaultPaymentResumeHandlerTest {
    @MockK
    private lateinit var config: PrimerConfig

    private val payment = mockk<Payment>()
    private val paymentDecision = PaymentDecision.Success(payment)

    private lateinit var defaultPaymentResumeHandler: DefaultPaymentResumeHandler
    private val resumePaymentInteractor = mockk<ResumePaymentInteractor>(relaxed = true)

    private val postResumeHandler =
        mockk<PostResumeHandler>(relaxed = true) {
            coEvery { handle(any()) } returns Result.success(PaymentDecision.Success(payment))
        }

    @BeforeEach
    fun setUp() {
        DISdkContext.headlessSdkContainer =
            mockk<SdkContainer>(relaxed = true).also { sdkContainer ->
                val cont =
                    spyk<DependencyContainer>().also { container ->
                        container.registerFactory<ResumePaymentInteractor>(
                            name = PaymentsContainer.RESUME_PAYMENT_INTERACTOR_DI_KEY,
                        ) { resumePaymentInteractor }
                        container.registerFactory<PostResumeHandler> { postResumeHandler }
                    }

                every { sdkContainer.containers }.returns(mutableMapOf(cont::class.simpleName.orEmpty() to cont))
            }

        defaultPaymentResumeHandler = DefaultPaymentResumeHandler(config)
    }

    @Test
    fun `handle should use AUTO strategy when payment handling is auto`() =
        runTest {
            // Arrange
            val resumeToken = "resumeToken"
            val paymentId = "paymentId"

            every { config.settings.paymentHandling } returns PrimerPaymentHandling.AUTO

            // Act
            val result = defaultPaymentResumeHandler.handle(resumeToken, paymentId)

            // Assert
            assertTrue(result.isSuccess)
//        assertEquals(paymentDecision, result.getOrNull())
            coVerify { resumePaymentInteractor.invoke(ResumeParams(paymentId, resumeToken)) }
        }

    @Test
    fun `handle should use MANUAL strategy when payment handling is manual`() =
        runTest {
            // Arrange
            val resumeToken = "resumeToken"
            val paymentDecision = PaymentDecision.Success(payment)

            every { config.settings.paymentHandling } returns PrimerPaymentHandling.MANUAL

            // Act
            val result = defaultPaymentResumeHandler.handle(resumeToken, null)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(paymentDecision, result.getOrNull())
            coVerify { postResumeHandler.handle(resumeToken) }
        }
}
