package io.primer.android.paymentmethods.analytics.delegate

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
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
            primerPaymentMethodManagerCategory = primerPaymentMethodManagerCategory.name,
            analyticsInteractor = analyticsInteractor
        )
    }

    @Test
    fun `logSdkAnalyticsEvent should log SdkFunctionParams via AnalyticsInteractor when called`() = runTest {
        val methodName = "methodName"
        val context = mapOf("key" to "value")

        coEvery { analyticsInteractor(any()) } returns Result.success(Unit)

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
