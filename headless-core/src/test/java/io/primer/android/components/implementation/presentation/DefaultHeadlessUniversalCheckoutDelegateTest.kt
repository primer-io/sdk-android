package io.primer.android.components.implementation.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.components.implementation.core.paymentmethods.composer.registry.PrimerPaymentMethodComposerRegistry
import io.primer.android.components.implementation.core.paymentmethods.composer.registry.PrimerVaultedPaymentMethodComposerRegistry
import io.primer.android.components.implementation.domain.PaymentsTypesInteractor
import io.primer.android.configuration.data.datasource.GlobalCacheConfigurationCacheDataSource
import io.primer.android.core.domain.None
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
internal class DefaultHeadlessUniversalCheckoutDelegateTest {

    @MockK
    lateinit var paymentsTypesInteractor: PaymentsTypesInteractor

    @MockK
    lateinit var analyticsInteractor: AnalyticsInteractor

    @MockK(relaxed = true)
    lateinit var globalCacheConfigurationCacheDataSource: GlobalCacheConfigurationCacheDataSource

    @MockK
    lateinit var errorHandler: CheckoutErrorHandler

    @MockK
    lateinit var baseErrorResolver: BaseErrorResolver

    private val paymentMethodComposerRegistry = PrimerPaymentMethodComposerRegistry()

    private val vaultedPaymentMethodComposerRegistry = PrimerVaultedPaymentMethodComposerRegistry()

    private fun delegate(scope: CoroutineScope) = DefaultHeadlessUniversalCheckoutDelegate(
        paymentsTypesInteractor = paymentsTypesInteractor,
        analyticsInteractor = analyticsInteractor,
        globalCacheConfigurationCacheDataSource = globalCacheConfigurationCacheDataSource,
        errorHandler = errorHandler,
        baseErrorResolver = baseErrorResolver,
        scopeProvider = object : CoroutineScopeProvider {
            override val scope: CoroutineScope = scope
        },
        paymentMethodComposerRegistry = paymentMethodComposerRegistry,
        vaultedPaymentMethodComposerRegistry = vaultedPaymentMethodComposerRegistry,
    )

    @Test
    fun `start should surface an init failure through the error handler`() = runTest {
        val throwable = RuntimeException("configuration fetch failed")
        val error = mockk<PrimerError>()
        coEvery { analyticsInteractor(any()) } returns Result.success(Unit)
        coEvery { paymentsTypesInteractor(None) } returns Result.failure(throwable)
        every { baseErrorResolver.resolve(throwable) } returns error
        coEvery { errorHandler.handle(any(), any()) } returns Unit

        delegate(this).start()
        advanceUntilIdle()

        coVerify(exactly = 1) { errorHandler.handle(error = error, payment = null) }
    }

    @Test
    fun `start should not invoke the error handler when init succeeds`() = runTest {
        coEvery { analyticsInteractor(any()) } returns Result.success(Unit)
        coEvery { paymentsTypesInteractor(None) } returns Result.success(Unit)

        delegate(this).start()
        advanceUntilIdle()

        coVerify(exactly = 0) { errorHandler.handle(any(), any()) }
    }

    @Test
    fun `clear should cancel and unregister every registered composer`() {
        val composer = mockk<PaymentMethodComposer>(relaxed = true)
        val vaultedComposer = mockk<PaymentMethodComposer>(relaxed = true)
        paymentMethodComposerRegistry.register("PAYMENT_CARD", composer)
        vaultedPaymentMethodComposerRegistry.register("PAYMENT_CARD", vaultedComposer)

        delegate(CoroutineScope(SupervisorJob())).clear(exception = null, cleanClientSessionCache = false)

        verify(exactly = 1) { composer.cancel() }
        verify(exactly = 1) { vaultedComposer.cancel() }
        assertTrue(paymentMethodComposerRegistry.composers.isEmpty())
        assertTrue(vaultedPaymentMethodComposerRegistry.composers.isEmpty())
    }

    @Test
    fun `clear should cancel in-flight work of the headless scope`() {
        val scope = CoroutineScope(SupervisorJob())
        val job = scope.launch { kotlinx.coroutines.awaitCancellation() }

        delegate(scope).clear(exception = null, cleanClientSessionCache = false)

        assertTrue(job.isCancelled)
    }
}
