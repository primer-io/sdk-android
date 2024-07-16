package io.primer.android.completion

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.domain.error.ErrorMapperFactory
import io.primer.android.domain.mock.repository.MockConfigurationRepository
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.payments.helpers.StripeAchPostPaymentCreationEventResolver
import io.primer.android.domain.payments.methods.repository.PaymentMethodDescriptorsRepository
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.domain.respository.ThreeDsRepository
import io.primer.android.threeds.helpers.ThreeDsLibraryVersionValidator
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertIs

@ExtendWith(MockKExtension::class)
class ResumeHandlerFactoryTest {

    @MockK
    private lateinit var paymentMethodDescriptorsRepository: PaymentMethodDescriptorsRepository

    @MockK
    private lateinit var validationTokenRepository: ValidateTokenRepository

    @MockK
    private lateinit var clientTokenRepository: ClientTokenRepository

    @MockK
    private lateinit var paymentMethodRepository: PaymentMethodRepository

    @MockK
    private lateinit var paymentResultRepository: PaymentResultRepository

    @MockK
    private lateinit var analyticsRepository: AnalyticsRepository

    @MockK
    private lateinit var mockConfigurationRepository: MockConfigurationRepository

    @MockK
    private lateinit var threeDsSdkClassValidator: ThreeDsSdkClassValidator

    @MockK
    private lateinit var threeDsLibraryVersionValidator: ThreeDsLibraryVersionValidator

    @MockK
    private lateinit var errorEventResolver: BaseErrorEventResolver

    @MockK
    private lateinit var eventDispatcher: EventDispatcher

    @MockK
    private lateinit var threeDsRepository: ThreeDsRepository

    @MockK
    private lateinit var errorMapperFactory: ErrorMapperFactory

    @MockK
    private lateinit var logReporter: LogReporter

    @MockK
    private lateinit var config: PrimerConfig

    @MockK
    private lateinit var retailOutletRepository: RetailOutletRepository

    @MockK
    private lateinit var stripeAchPostPaymentCreationEventResolver: StripeAchPostPaymentCreationEventResolver

    @MockK
    private lateinit var asyncPaymentMethodDeeplinkRepository: AsyncPaymentMethodDeeplinkRepository

    @InjectMockKs
    private lateinit var factory: ResumeHandlerFactory

    @Test
    fun `getResumeHandler() should return resume handler when descriptor list is not empty`() {
        val mockResumeHandler = mockk<DefaultPrimerResumeDecisionHandler>()
        every { paymentMethodRepository.getPaymentMethod().paymentMethodType } returns "STRIPE_ACH"
        val descriptor = mockk<PaymentMethodDescriptor> {
            every { config.type } returns "STRIPE_ACH"
            every { resumeHandler } returns mockResumeHandler
        }
        every { paymentMethodDescriptorsRepository.getPaymentMethodDescriptors() } returns listOf(descriptor)

        val handler = factory.getResumeHandler("STRIPE_ACH")

        assertEquals(mockResumeHandler, handler)
    }

    @Test
    fun `getResumeHandler() should return ThreeDsPrimerResumeDecisionHandler when descriptor list is empty and payment instrument type is PAYMENT_CARD`() {
        every { paymentMethodDescriptorsRepository.getPaymentMethodDescriptors() } returns emptyList()

        val handler = factory.getResumeHandler("PAYMENT_CARD")

        assertIs<ThreeDsPrimerResumeDecisionHandler>(handler)
    }

    @Test
    fun `getResumeHandler() should return ThreeDsPrimerResumeDecisionHandler when descriptor list is empty and payment instrument type is GOOGLE_PAY`() {
        every { paymentMethodDescriptorsRepository.getPaymentMethodDescriptors() } returns emptyList()

        val handler = factory.getResumeHandler(PaymentMethodType.GOOGLE_PAY.name)

        assertIs<ThreeDsPrimerResumeDecisionHandler>(handler)
    }

    @Test
    fun `getResumeHandler() should return AsyncPaymentPrimerResumeDecisionHandler when descriptor list is empty and payment instrument type is OFF_SESSION_PAYMENT`() {
        every { paymentMethodDescriptorsRepository.getPaymentMethodDescriptors() } returns emptyList()

        val handler = factory.getResumeHandler("OFF_SESSION_PAYMENT")

        assertIs<AsyncPaymentPrimerResumeDecisionHandler>(handler)
    }

    @Test
    fun `getResumeHandler() should return AsyncPaymentPrimerResumeDecisionHandler when descriptor list is empty and payment instrument type is CARD_OFF_SESSION_PAYMENT`() {
        every { paymentMethodDescriptorsRepository.getPaymentMethodDescriptors() } returns emptyList()

        val handler = factory.getResumeHandler("CARD_OFF_SESSION_PAYMENT")

        assertIs<AsyncPaymentPrimerResumeDecisionHandler>(handler)
    }

    @Test
    fun `getResumeHandler() should return DefaultPrimerResumeDecisionHandler when descriptor list is empty and payment instrument type is STRIPE_ACH`() {
        every { paymentMethodDescriptorsRepository.getPaymentMethodDescriptors() } returns emptyList()

        val handler = factory.getResumeHandler("STRIPE_ACH")

        assertIs<DefaultPrimerResumeDecisionHandler>(handler)
    }
}
