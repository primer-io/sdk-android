package io.primer.android.components.implementation.completion

import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.PrimerHeadlessUniversalCheckoutUiListener
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class DefaultPreparationStartHandlerTest {
    @RelaxedMockK
    internal lateinit var analyticsRepository: AnalyticsRepository

    private lateinit var startHandler: DefaultPreparationStartHandler

    @BeforeEach
    fun setUp() {
        startHandler = DefaultPreparationStartHandler(analyticsRepository = analyticsRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `handle should emit payment to paymentMethodType flow`() =
        runTest {
            val paymentMethodType = "paymentMethodType"

            val job =
                launch {
                    val emittedPaymentMethodType = startHandler.preparationStarted.first()
                    assertEquals(paymentMethodType, emittedPaymentMethodType)
                }

            startHandler.handle(paymentMethodType)
            job.cancel()
        }

    @Test
    fun `handle should call onCheckoutCompleted with correct data`() {
        val paymentMethodType = "paymentMethodType"

        val checkoutUiListener = mockk<PrimerHeadlessUniversalCheckoutUiListener>(relaxed = true)
        mockkObject(PrimerHeadlessUniversalCheckout)
        every { PrimerHeadlessUniversalCheckout.instance.uiListener } returns checkoutUiListener

        runTest {
            startHandler.handle(paymentMethodType)
        }

        coVerify {
            checkoutUiListener.onPreparationStarted(paymentMethodType)
        }
        verify {
            analyticsRepository.addEvent(
                SdkFunctionParams(
                    HeadlessUniversalCheckoutAnalyticsConstants.ON_PREPARATION_STARTED,
                    mapOf(
                        HeadlessUniversalCheckoutAnalyticsConstants.PAYMENT_METHOD_TYPE
                            to paymentMethodType,
                    ),
                ),
            )
        }

        unmockkObject(PrimerHeadlessUniversalCheckout)
    }
}
