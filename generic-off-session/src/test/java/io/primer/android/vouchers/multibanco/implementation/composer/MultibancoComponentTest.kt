package io.primer.android.vouchers.multibanco.implementation.composer

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.vouchers.multibanco.implementation.payment.delegate.MultibancoPaymentDelegate
import io.primer.android.vouchers.multibanco.implementation.tokenization.presentation.MultibancoTokenizationDelegate
import io.primer.android.vouchers.multibanco.implementation.tokenization.presentation.composable.MultibancoTokenizationInputable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class MultibancoComponentTest {

    private val tokenizationDelegate: MultibancoTokenizationDelegate = mockk()
    private val paymentDelegate: MultibancoPaymentDelegate = mockk()
    private lateinit var multibancoComponent: MultibancoComponent
    private val testDispatcher = TestCoroutineDispatcher()

    @BeforeEach
    fun setup() {
        DISdkContext.headlessSdkContainer = mockk<SdkContainer>(relaxed = true).also { sdkContainer ->
            val cont = spyk<DependencyContainer>().also { container ->
                container.registerFactory<CoroutineScopeProvider> {
                    object : CoroutineScopeProvider {
                        override val scope: CoroutineScope
                            get() = TestScope()
                    }
                }
            }
            every { sdkContainer.containers }.returns(mutableMapOf(cont::class.simpleName.orEmpty() to cont))
        }
        Dispatchers.setMain(testDispatcher)
        multibancoComponent = MultibancoComponent(tokenizationDelegate, paymentDelegate)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `start should initialize paymentMethodType and primerSessionIntent and call startTokenization`() = runTest {
        val paymentMethodType = "multibanco"
        val primerSessionIntent: PrimerSessionIntent = mockk(relaxed = true)
        val paymentDecision = mockk<PaymentDecision>()

        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.success(mockk())
        coEvery { paymentDelegate.handlePaymentMethodToken(any(), any()) } returns Result.success(paymentDecision)

        multibancoComponent.start(paymentMethodType, primerSessionIntent)

        coVerify { tokenizationDelegate.tokenize(any()) }
        coVerify { paymentDelegate.handlePaymentMethodToken(any(), any()) }
    }

    @Test
    fun `startTokenization should call tokenizationDelegate and paymentDelegate on success`() = runTest {
        val paymentMethodType = "multibanco"
        val primerSessionIntent: PrimerSessionIntent = mockk(relaxed = true)
        val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
        val paymentDecision = mockk<PaymentDecision>()

        val inputable = MultibancoTokenizationInputable(paymentMethodType, primerSessionIntent)

        coEvery { tokenizationDelegate.tokenize(inputable) } returns Result.success(paymentMethodTokenData)
        coEvery {
            paymentDelegate.handlePaymentMethodToken(paymentMethodTokenData, primerSessionIntent)
        } returns Result.success(paymentDecision)

        multibancoComponent.start(paymentMethodType, primerSessionIntent)

        coVerify { tokenizationDelegate.tokenize(inputable) }
        coVerify { paymentDelegate.handlePaymentMethodToken(paymentMethodTokenData, primerSessionIntent) }
    }

    @Test
    fun `startTokenization should handle error when tokenizationDelegate fails`() = runTest {
        val paymentMethodType = "multibanco"
        val primerSessionIntent: PrimerSessionIntent = mockk(relaxed = true)
        val exception = Exception("Tokenization failed")
        val inputable = MultibancoTokenizationInputable(paymentMethodType, primerSessionIntent)

        coEvery { tokenizationDelegate.tokenize(inputable) } returns Result.failure(exception)
        coEvery { paymentDelegate.handleError(exception) } returns Unit

        multibancoComponent.start(paymentMethodType, primerSessionIntent)

        coVerify { tokenizationDelegate.tokenize(inputable) }
        coVerify { paymentDelegate.handleError(exception) }
    }
}
