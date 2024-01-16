package io.primer.android.components.presentation.paymentMethods.analytics.delegate

import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.domain.error.models.PrimerError
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PaymentMethodSdkAnalyticsEventLoggingDelegateTest {
    private val paymentMethodType = "paymentMethodType"

    @MockK
    private lateinit var primerPaymentMethodManagerCategory: PrimerPaymentMethodManagerCategory

    @MockK
    private lateinit var analyticsInteractor: AnalyticsInteractor

    private lateinit var delegate: PaymentMethodSdkAnalyticsEventLoggingDelegate

    @BeforeEach
    fun setUp() {
        every { primerPaymentMethodManagerCategory.name } returns "categoryName"

        delegate = PaymentMethodSdkAnalyticsEventLoggingDelegate(
            primerPaymentMethodManagerCategory = primerPaymentMethodManagerCategory,
            analyticsInteractor = analyticsInteractor
        )
    }

    @Test
    fun `logSdkAnalyticsEvent should log SdkFunctionParams via AnalyticsInteractor when called`() = runTest {
        val methodName = "methodName"
        val context = mapOf("key" to "value")
        val primerError = mockk<PrimerError> {
            every { description } returns "description"
            every { diagnosticsId } returns "diagnosticsId"
        }
        every { analyticsInteractor(any()) } returns emptyFlow()

        delegate.logSdkAnalyticsEvent(
            paymentMethodType = paymentMethodType,
            methodName = methodName,
            context = context
        )

        coVerify(exactly = 1) {
            analyticsInteractor(
                SdkFunctionParams(
                    name = methodName,
                    params = mapOf(
                        "paymentMethodType" to paymentMethodType,
                        "category" to "categoryName"
                    ) + context
                )
            )
        }
    }
}
