@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.sandboxProcessor.implementation.components

import io.mockk.Awaits
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.sandboxProcessor.InstantExecutorExtension
import io.primer.android.sandboxProcessor.SandboxProcessorDecisionType
import io.primer.android.sandboxProcessor.implementation.payment.delegate.SandboxProcessorPaymentDelegate
import io.primer.android.sandboxProcessor.implementation.tokenization.presentation.SandboxProcessorTokenizationDelegate
import io.primer.android.sandboxProcessor.implementation.tokenization.presentation.composable.SandboxProcessorTokenizationInputable
import io.primer.android.sandboxProcessor.toListDuring
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Duration.Companion.seconds

@ExtendWith(MockKExtension::class, InstantExecutorExtension::class)
internal class ProcessorTestComponentTest {

    @MockK
    lateinit var tokenizationDelegate: SandboxProcessorTokenizationDelegate

    @MockK
    lateinit var paymentDelegate: SandboxProcessorPaymentDelegate

    private val primerSessionIntent = PrimerSessionIntent.CHECKOUT

    private lateinit var component: ProcessorTestComponent

    @BeforeEach
    fun setUp() {
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
        component = ProcessorTestComponent(tokenizationDelegate, paymentDelegate)
    }

    @Test
    fun `start() method should call tokenize() and handlePaymentMethodToken() when tokenization is successful`() = runTest {
        val primerPaymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.success(primerPaymentMethodTokenData)
        val paymentDecision = mockk<PaymentDecision>()
        coEvery { paymentDelegate.handlePaymentMethodToken(any(), any()) } returns Result.success(paymentDecision)
        component.updateCollectedData(ProcessorTestCollectableData(decisionType = SandboxProcessorDecisionType.SUCCESS))

        val list = async { component.componentStep.toListDuring(5.seconds) }
        launch {
            component.start("paymentMethodType", primerSessionIntent)
        }

        assertEquals(
            listOf(
                ProcessorTestStep.Started,
                ProcessorTestStep.Tokenized,
                ProcessorTestStep.Finished
            ),
            list.await()
        )
        coVerify {
            tokenizationDelegate.tokenize(
                SandboxProcessorTokenizationInputable(
                    paymentMethodType = "paymentMethodType",
                    primerSessionIntent = primerSessionIntent,
                    decisionType = SandboxProcessorDecisionType.SUCCESS
                )
            )
            paymentDelegate.handlePaymentMethodToken(
                paymentMethodTokenData = primerPaymentMethodTokenData,
                primerSessionIntent = primerSessionIntent
            )
        }
        confirmVerified(paymentDecision)
    }

    @Test
    fun `start() method should not call tokenize() and call handleError() when data is not collected`() = runTest {
        coEvery { paymentDelegate.handleError(any()) } just Awaits
        val list = async { component.componentStep.toListDuring(5.seconds) }
        launch {
            component.start("paymentMethodType", primerSessionIntent)
        }

        assertEquals(
            listOf(
                ProcessorTestStep.Started
            ),
            list.await()
        )
        coVerify(exactly = 0) {
            tokenizationDelegate.tokenize(any())
        }
        coVerify { paymentDelegate.handleError(any()) }
    }

    @Test
    fun `start() method should call tokenize() and handleError() when tokenization fails`() = runTest {
        coEvery { paymentDelegate.handleError(any()) } just Awaits
        component.updateCollectedData(ProcessorTestCollectableData(decisionType = SandboxProcessorDecisionType.SUCCESS))
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.failure(Exception())

        val list = async { component.componentStep.toListDuring(5.seconds) }
        launch {
            component.start("paymentMethodType", primerSessionIntent)
        }

        assertEquals(
            listOf(
                ProcessorTestStep.Started
            ),
            list.await()
        )
        coVerify {
            tokenizationDelegate.tokenize(
                SandboxProcessorTokenizationInputable(
                    paymentMethodType = "paymentMethodType",
                    primerSessionIntent = primerSessionIntent,
                    decisionType = SandboxProcessorDecisionType.SUCCESS
                )
            )
            paymentDelegate.handleError(any())
        }
        coVerify(exactly = 0) {
            paymentDelegate.handlePaymentMethodToken(any(), any())
        }
    }

    @Test
    fun `start() method should call tokenize(), handlePaymentMethodToken() and handleError() when handlePaymentMethodToken() fails`() = runTest {
        coEvery { paymentDelegate.handleError(any()) } just Awaits
        component.updateCollectedData(ProcessorTestCollectableData(decisionType = SandboxProcessorDecisionType.SUCCESS))
        val primerPaymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.success(primerPaymentMethodTokenData)
        coEvery { paymentDelegate.handlePaymentMethodToken(any(), any()) } returns Result.failure(Exception())

        val list = async { component.componentStep.toListDuring(5.seconds) }
        launch {
            component.start("paymentMethodType", primerSessionIntent)
        }

        assertEquals(
            listOf(
                ProcessorTestStep.Started,
                ProcessorTestStep.Tokenized
            ),
            list.await()
        )
        coVerify {
            tokenizationDelegate.tokenize(
                SandboxProcessorTokenizationInputable(
                    paymentMethodType = "paymentMethodType",
                    primerSessionIntent = primerSessionIntent,
                    decisionType = SandboxProcessorDecisionType.SUCCESS
                )
            )
            paymentDelegate.handlePaymentMethodToken(
                paymentMethodTokenData = primerPaymentMethodTokenData,
                primerSessionIntent = primerSessionIntent
            )
            paymentDelegate.handleError(any())
        }
    }
}
