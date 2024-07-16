package io.primer.android.domain.payments.additionalInfo

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientToken
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.ui.fragments.SuccessType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class AchAdditionalInfoResolverTest {
    @MockK
    private lateinit var clientToken: ClientToken

    @MockK
    private lateinit var extraParams:
        AchAdditionalInfoResolver.AchAdditionalInfoResolverExtraParams

    @MockK
    private lateinit var eventDispatcher: EventDispatcher

    @MockK
    private lateinit var paymentResultRepository: PaymentResultRepository

    @MockK
    private lateinit var checkoutErrorEventResolver: BaseErrorEventResolver

    @MockK
    private lateinit var config: PrimerConfig

    @MockK
    private lateinit var completeStripeAchPaymentSessionDelegate:
        CompleteStripeAchPaymentSessionDelegate

    @MockK
    private lateinit var stripeAchMandateTimestampLoggingDelegate:
        StripeAchMandateTimestampLoggingDelegate

    @InjectMockKs
    private lateinit var resolver: AchAdditionalInfoResolver

    @AfterEach
    fun tearDown() {
        confirmVerified(
            clientToken,
            extraParams,
            eventDispatcher,
            paymentResultRepository,
            checkoutErrorEventResolver,
            completeStripeAchPaymentSessionDelegate,
            stripeAchMandateTimestampLoggingDelegate,
            config
        )
    }

    @Test
    fun `resolve() should return DisplayMandate and onAcceptMandate() should dispatch PaymentSuccess and ShowSuccess when client token has required data and delegate calls succeed (CHECKOUT)`() = runTest {
        every { config.intent.paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
        every { clientToken.stripePaymentIntentId } returns "stripePaymentIntentId"
        every { clientToken.sdkCompleteUrl } returns "sdkCompleteUrl"
        val dateSlot = slot<Date>()
        coEvery {
            stripeAchMandateTimestampLoggingDelegate.logTimestamp(
                stripePaymentIntentId = any(),
                date = capture(dateSlot)
            )
        } just Runs
        coEvery {
            completeStripeAchPaymentSessionDelegate.invoke(
                completeUrl = any(),
                paymentMethodId = any(),
                mandateTimestamp = any()
            )
        } returns Result.success(Unit)
        every { extraParams.paymentMethodId } returns "paymentMethodId"
        every { eventDispatcher.dispatchEvent(any()) } just Runs
        val payment = mockk<Payment>()
        every { paymentResultRepository.getPaymentResult().payment } returns payment

        val result = resolver.resolve(clientToken = clientToken, extraParams = extraParams)
        result.onAcceptMandate()

        assertInstanceOf(AchAdditionalInfo.DisplayMandate::class.java, result)
        coVerify {
            stripeAchMandateTimestampLoggingDelegate.logTimestamp(
                stripePaymentIntentId = "stripePaymentIntentId",
                date = dateSlot.captured
            )
            completeStripeAchPaymentSessionDelegate(
                completeUrl = "sdkCompleteUrl",
                paymentMethodId = "paymentMethodId",
                mandateTimestamp = dateSlot.captured
            )
        }
        verify {
            clientToken.stripePaymentIntentId
            clientToken.sdkCompleteUrl
            extraParams.paymentMethodId
            eventDispatcher.dispatchEvent(
                withArg {
                    assertInstanceOf(CheckoutEvent.PaymentSuccess::class.java, it)
                    assertEquals(
                        PrimerCheckoutData(payment = payment),
                        (it as CheckoutEvent.PaymentSuccess).data
                    )
                }
            )
            eventDispatcher.dispatchEvent(
                withArg {
                    assertInstanceOf(CheckoutEvent.ShowSuccess::class.java, it)
                    assertEquals(
                        SuccessType.PAYMENT_SUCCESS,
                        (it as CheckoutEvent.ShowSuccess).successType
                    )
                }
            )
            paymentResultRepository.getPaymentResult().payment
            config.intent.paymentMethodIntent
        }
    }

    @Test
    fun `resolve() should return DisplayMandate and onAcceptMandate() should dispatch PaymentSuccess and ShowSuccess when client token has required data and delegate calls succeed (VAULT)`() = runTest {
        every { config.intent.paymentMethodIntent } returns PrimerSessionIntent.VAULT
        every { clientToken.stripePaymentIntentId } returns "stripePaymentIntentId"
        every { clientToken.sdkCompleteUrl } returns "sdkCompleteUrl"
        val dateSlot = slot<Date>()
        coEvery {
            stripeAchMandateTimestampLoggingDelegate.logTimestamp(
                stripePaymentIntentId = any(),
                date = capture(dateSlot)
            )
        } just Runs
        coEvery {
            completeStripeAchPaymentSessionDelegate.invoke(
                completeUrl = any(),
                paymentMethodId = any(),
                mandateTimestamp = any()
            )
        } returns Result.success(Unit)
        every { extraParams.paymentMethodId } returns "paymentMethodId"
        every { eventDispatcher.dispatchEvent(any()) } just Runs
        val payment = mockk<Payment>()
        every { paymentResultRepository.getPaymentResult().payment } returns payment

        val result = resolver.resolve(clientToken = clientToken, extraParams = extraParams)
        result.onAcceptMandate()

        assertInstanceOf(AchAdditionalInfo.DisplayMandate::class.java, result)
        coVerify {
            stripeAchMandateTimestampLoggingDelegate.logTimestamp(
                stripePaymentIntentId = "stripePaymentIntentId",
                date = dateSlot.captured
            )
            completeStripeAchPaymentSessionDelegate(
                completeUrl = "sdkCompleteUrl",
                paymentMethodId = "paymentMethodId",
                mandateTimestamp = dateSlot.captured
            )
        }
        verify {
            clientToken.stripePaymentIntentId
            clientToken.sdkCompleteUrl
            extraParams.paymentMethodId
            eventDispatcher.dispatchEvent(
                withArg {
                    assertInstanceOf(CheckoutEvent.PaymentSuccess::class.java, it)
                    assertEquals(
                        PrimerCheckoutData(payment = payment),
                        (it as CheckoutEvent.PaymentSuccess).data
                    )
                }
            )
            eventDispatcher.dispatchEvent(
                withArg {
                    assertInstanceOf(CheckoutEvent.ShowSuccess::class.java, it)
                    assertEquals(
                        SuccessType.VAULT_TOKENIZATION_SUCCESS,
                        (it as CheckoutEvent.ShowSuccess).successType
                    )
                }
            )
            paymentResultRepository.getPaymentResult().payment
            config.intent.paymentMethodIntent
        }
    }

    @Test
    fun `resolve() should return DisplayMandate and onAcceptMandate() should dispatch error when client token has required data but delegate call fails`() = runTest {
        every { clientToken.stripePaymentIntentId } returns "stripePaymentIntentId"
        every { clientToken.sdkCompleteUrl } returns "sdkCompleteUrl"
        val dateSlot = slot<Date>()
        coEvery {
            stripeAchMandateTimestampLoggingDelegate.logTimestamp(
                stripePaymentIntentId = any(),
                date = capture(dateSlot)
            )
        } just Runs
        val error = Exception()
        coEvery {
            completeStripeAchPaymentSessionDelegate.invoke(
                completeUrl = any(),
                paymentMethodId = any(),
                mandateTimestamp = any()
            )
        } returns Result.failure(error)
        every { checkoutErrorEventResolver.resolve(any(), any()) } just Runs

        val result = resolver.resolve(clientToken = clientToken, extraParams = AdditionalInfoResolverExtraParams.None)
        result.onAcceptMandate()

        assertInstanceOf(AchAdditionalInfo.DisplayMandate::class.java, result)
        coVerify {
            stripeAchMandateTimestampLoggingDelegate.logTimestamp(
                stripePaymentIntentId = "stripePaymentIntentId",
                date = dateSlot.captured
            )
            completeStripeAchPaymentSessionDelegate(
                completeUrl = "sdkCompleteUrl",
                paymentMethodId = null,
                mandateTimestamp = dateSlot.captured
            )
        }
        verify {
            clientToken.stripePaymentIntentId
            clientToken.sdkCompleteUrl
            checkoutErrorEventResolver.resolve(
                throwable = error,
                type = ErrorMapperType.DEFAULT
            )
        }
    }

    @Test
    fun `resolve() should return DisplayMandate and onAcceptMandate() should dispatch error when client token misses stripePaymentIntentId`() = runTest {
        every { clientToken.stripePaymentIntentId } returns null
        every { checkoutErrorEventResolver.resolve(any(), any()) } just Runs

        val result = resolver.resolve(clientToken = clientToken, extraParams = extraParams)
        result.onAcceptMandate()

        assertInstanceOf(AchAdditionalInfo.DisplayMandate::class.java, result)
        verify {
            clientToken.stripePaymentIntentId
            checkoutErrorEventResolver.resolve(
                throwable = any(),
                type = ErrorMapperType.DEFAULT
            )
        }
    }

    @Test
    fun `resolve() should return DisplayMandate and onAcceptMandate() should dispatch error when client token misses completeUrl`() = runTest {
        every { clientToken.stripePaymentIntentId } returns "stripePaymentIntentId"
        every { clientToken.sdkCompleteUrl } returns null
        every { checkoutErrorEventResolver.resolve(any(), any()) } just Runs

        val result = resolver.resolve(clientToken = clientToken, extraParams = extraParams)
        result.onAcceptMandate()

        assertInstanceOf(AchAdditionalInfo.DisplayMandate::class.java, result)
        verify {
            clientToken.stripePaymentIntentId
            clientToken.sdkCompleteUrl
            checkoutErrorEventResolver.resolve(
                throwable = any(),
                type = ErrorMapperType.DEFAULT
            )
        }
    }

    @Test
    fun `resolve() should return DisplayMandate and onDeclineMandate() should dispatch PaymentMethodCancelledException`() = runTest {
        every { checkoutErrorEventResolver.resolve(any(), any()) } just Runs

        val result = resolver.resolve(clientToken = clientToken, extraParams = extraParams)
        result.onDeclineMandate()

        assertInstanceOf(AchAdditionalInfo.DisplayMandate::class.java, result)
        verify {
            checkoutErrorEventResolver.resolve(
                throwable = PaymentMethodCancelledException(
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name
                ),
                type = ErrorMapperType.DEFAULT
            )
        }
    }
}
