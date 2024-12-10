package io.primer.android.components

import android.content.Context
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.implementation.core.presentation.PaymentMethodInitializer
import io.primer.android.components.implementation.core.presentation.PaymentMethodStarter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class DefaultPaymentMethodManagerDelegateTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var paymentMethodInitializer: PaymentMethodInitializer
    private lateinit var paymentMethodStarter: PaymentMethodStarter
    private lateinit var paymentMethodManagerDelegate: DefaultPaymentMethodManagerDelegate

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        paymentMethodInitializer = mockk(relaxed = true)
        paymentMethodStarter = mockk(relaxed = true)
        paymentMethodManagerDelegate = DefaultPaymentMethodManagerDelegate(
            paymentMethodInitializer = paymentMethodInitializer,
            paymentMethodStarter = paymentMethodStarter
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should call paymentMethodInitializer with correct parameters`() = runTest {
        val paymentMethodType = "sampleType"
        val category = PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT

        paymentMethodManagerDelegate.init(paymentMethodType, category)

        coVerify {
            paymentMethodInitializer.init(paymentMethodType, category)
        }
    }

    @Test
    fun `start should call paymentMethodStarter with correct parameters`() = runTest {
        val context = mockk<Context>(relaxed = true)
        val paymentMethodType = "sampleType"
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val category = PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT

        paymentMethodManagerDelegate.start(context, paymentMethodType, sessionIntent, category)

        coVerify {
            paymentMethodStarter.start(context, paymentMethodType, sessionIntent, category, any())
        }
    }
}
