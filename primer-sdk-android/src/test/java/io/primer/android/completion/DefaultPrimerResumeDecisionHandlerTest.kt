package io.primer.android.completion

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientToken
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.payments.helpers.StripeAchPostPaymentCreationEventResolver
import io.primer.android.domain.payments.methods.repository.PaymentMethodDescriptorsRepository
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class DefaultPrimerResumeDecisionHandlerTest {
    @RelaxedMockK
    private lateinit var validationTokenRepository: ValidateTokenRepository

    @RelaxedMockK
    private lateinit var clientTokenRepository: ClientTokenRepository

    @RelaxedMockK
    private lateinit var paymentMethodRepository: PaymentMethodRepository

    @RelaxedMockK
    private lateinit var paymentResultRepository: PaymentResultRepository

    @RelaxedMockK
    private lateinit var analyticsRepository: AnalyticsRepository

    @RelaxedMockK
    private lateinit var errorEventResolver: BaseErrorEventResolver

    @RelaxedMockK
    private lateinit var eventDispatcher: EventDispatcher

    @RelaxedMockK
    private lateinit var logReporter: LogReporter

    @RelaxedMockK
    private lateinit var config: PrimerConfig

    @RelaxedMockK
    private lateinit var paymentMethodDescriptorsRepository: PaymentMethodDescriptorsRepository

    @RelaxedMockK
    private lateinit var retailerOutletRepository: RetailOutletRepository

    @RelaxedMockK
    private lateinit var stripeAchPostPaymentCreationEventResolver:
        StripeAchPostPaymentCreationEventResolver

    private lateinit var handler: DefaultPrimerResumeDecisionHandler

    private fun createHandler(scheduler: TestCoroutineScheduler) = DefaultPrimerResumeDecisionHandler(
        validationTokenRepository = validationTokenRepository,
        clientTokenRepository = clientTokenRepository,
        paymentMethodRepository = paymentMethodRepository,
        paymentResultRepository = paymentResultRepository,
        analyticsRepository = analyticsRepository,
        errorEventResolver = errorEventResolver,
        eventDispatcher = eventDispatcher,
        logReporter = logReporter,
        config = config,
        paymentMethodDescriptorsRepository = paymentMethodDescriptorsRepository,
        retailerOutletRepository = retailerOutletRepository,
        stripeAchPostPaymentCreationEventResolver = stripeAchPostPaymentCreationEventResolver,
        dispatcher = StandardTestDispatcher(scheduler)
    )

    @Test
    fun `continueWithNewClientToken() should delegate to stripeAchPostPaymentCreationEventResolver when paymentMethodType is STRIPE_ACH`() = runTest {
        handler = createHandler(testScheduler)
        mockkObject(ClientToken.Companion)
        val rawClientToken = "token"
        val clientToken = mockk<ClientToken>()
        every { ClientToken.fromString(rawClientToken) } returns clientToken
        every {
            paymentMethodRepository.getPaymentMethod().paymentMethodType
        } returns PaymentMethodType.STRIPE_ACH.name
        coEvery {
            stripeAchPostPaymentCreationEventResolver.resolve(any(), any())
        } just Runs

        handler.continueWithNewClientToken(clientToken = rawClientToken)
        advanceUntilIdle()

        verify {
            ClientToken.fromString(rawClientToken)
        }
        coVerify {
            stripeAchPostPaymentCreationEventResolver.resolve(
                clientToken = clientToken,
                onBankSelected = any()
            )
        }
        confirmVerified(stripeAchPostPaymentCreationEventResolver, ClientToken.Companion)
        unmockkObject(ClientToken.Companion)
    }

    @Test
    fun `continueWithNewClientToken() should not delegate to stripeAchPostPaymentCreationEventResolver when paymentMethodType is not STRIPE_ACH`() = runTest {
        handler = createHandler(testScheduler)
        mockkObject(ClientToken.Companion)
        every {
            paymentMethodRepository.getPaymentMethod().paymentMethodType
        } returns PaymentMethodType.KLARNA.name

        handler.continueWithNewClientToken(clientToken = RAW_CLIENT_TOKEN)
        advanceUntilIdle()

        verify(exactly = 0) {
            ClientToken.fromString(any())
        }
        coVerify(exactly = 0) {
            stripeAchPostPaymentCreationEventResolver.resolve(
                clientToken = any(),
                onBankSelected = any()
            )
        }
        confirmVerified(stripeAchPostPaymentCreationEventResolver, ClientToken.Companion)
        unmockkObject(ClientToken.Companion)
    }

    @Test
    fun `continueWithNewClientToken() should not delegate to stripeAchPostPaymentCreationEventResolver when token parsing fails`() = runTest {
        handler = createHandler(testScheduler)
        mockkObject(ClientToken.Companion)
        val exception = IllegalArgumentException()
        every { ClientToken.fromString(any()) } throws exception
        every {
            paymentMethodRepository.getPaymentMethod().paymentMethodType
        } returns PaymentMethodType.STRIPE_ACH.name

        handler.continueWithNewClientToken(clientToken = RAW_CLIENT_TOKEN)
        advanceUntilIdle()

        verify(exactly = 1) {
            ClientToken.fromString(any())
            errorEventResolver.resolve(exception, ErrorMapperType.PAYMENT_RESUME)
        }
        coVerify(exactly = 0) {
            stripeAchPostPaymentCreationEventResolver.resolve(
                clientToken = any(),
                onBankSelected = any()
            )
        }
        confirmVerified(stripeAchPostPaymentCreationEventResolver, ClientToken.Companion, errorEventResolver)
        unmockkObject(ClientToken.Companion)
    }

    companion object {
        private const val RAW_CLIENT_TOKEN = "token"
    }
}
