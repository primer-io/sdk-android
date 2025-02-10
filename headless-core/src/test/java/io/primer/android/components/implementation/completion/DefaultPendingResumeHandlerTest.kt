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
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class DefaultPendingResumeHandlerTest {
    @RelaxedMockK
    internal lateinit var analyticsRepository: AnalyticsRepository

    private lateinit var pendingResumeHandler: DefaultPendingResumeHandler

    @BeforeEach
    fun setUp() {
        pendingResumeHandler = DefaultPendingResumeHandler(analyticsRepository = analyticsRepository)
    }

    @Test
    fun `handle should call onResumePending with correct data`() {
        val additionalInfo = mockk<PrimerCheckoutAdditionalInfo>(relaxed = true)

        val checkoutListener = mockk<PrimerHeadlessUniversalCheckoutListener>(relaxed = true)
        mockkObject(PrimerHeadlessUniversalCheckout)
        every { PrimerHeadlessUniversalCheckout.instance.checkoutListener } returns checkoutListener

        runTest {
            pendingResumeHandler.handle(additionalInfo)
        }

        coVerify {
            checkoutListener.onResumePending(additionalInfo)
        }
        verify {
            analyticsRepository.addEvent(
                SdkFunctionParams(
                    HeadlessUniversalCheckoutAnalyticsConstants.ON_CHECKOUT_PENDING,
                ),
            )
        }

        unmockkObject(PrimerHeadlessUniversalCheckout)
    }
}
