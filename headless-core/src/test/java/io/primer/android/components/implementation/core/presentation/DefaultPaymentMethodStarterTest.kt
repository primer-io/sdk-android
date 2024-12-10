package io.primer.android.components.implementation.core.presentation

import android.content.Context
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.InstantExecutorExtension
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.paymentmethods.core.composer.registry.PaymentMethodComposerRegistry
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.payments.core.helpers.PaymentMethodShowedHandler
import io.primer.android.webRedirectShared.implementation.composer.presentation.BaseWebRedirectComposer
import io.primer.paymentMethodCoreUi.core.ui.navigation.Navigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class DefaultPaymentMethodStarterTest {

    private lateinit var analyticsInteractor: AnalyticsInteractor
    private lateinit var composerRegistry: PaymentMethodComposerRegistry
    private lateinit var providerFactoryRegistry: PaymentMethodProviderFactoryRegistry
    private lateinit var paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry
    private lateinit var paymentMethodShowedHandler: PaymentMethodShowedHandler
    private lateinit var paymentMethodStarter: DefaultPaymentMethodStarter

    @BeforeEach
    fun setUp() {
        analyticsInteractor = mockk(relaxed = true)
        composerRegistry = mockk(relaxed = true)
        providerFactoryRegistry = mockk()
        paymentMethodNavigationFactoryRegistry = mockk()
        paymentMethodShowedHandler = mockk()
        paymentMethodStarter = DefaultPaymentMethodStarter(
            analyticsInteractor,
            composerRegistry,
            providerFactoryRegistry,
            paymentMethodNavigationFactoryRegistry,
            paymentMethodShowedHandler
        )
    }

    @Test
    fun `start should add analytics event`() {
        val context = mockk<Context>()
        val paymentMethodType = "testType"
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val category = PrimerPaymentMethodManagerCategory.NATIVE_UI

        val uiEventFlow = MutableSharedFlow<ComposerUiEvent>()
        val composer = mockk<BaseWebRedirectComposer>(relaxed = true) {
            every { uiEvent } returns uiEventFlow
        }
        every { providerFactoryRegistry.create(paymentMethodType, sessionIntent) } returns composer

        val navigationHandler = mockk<PaymentMethodContextNavigationHandler>(relaxed = true)
        every { paymentMethodNavigationFactoryRegistry.create(paymentMethodType) } returns navigationHandler

        coEvery { analyticsInteractor(any()) } returns Result.success(Unit)

        runTest {
            val job = launch {
                paymentMethodStarter.start(context, paymentMethodType, sessionIntent, category)
            }
            advanceUntilIdle()
            job.cancel()
        }

        coVerify {
            analyticsInteractor(any())
        }
    }

    @Test
    fun `start should unregister and register composer`() {
        val context = mockk<Context>()
        val paymentMethodType = "testType"
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val category = PrimerPaymentMethodManagerCategory.NATIVE_UI

        val uiEventFlow = MutableSharedFlow<ComposerUiEvent>(replay = 1)
        val composer = mockk<BaseWebRedirectComposer>(relaxed = true) {
            every { uiEvent } returns uiEventFlow
        }
        every { providerFactoryRegistry.create(paymentMethodType, sessionIntent) } returns composer
        coEvery { paymentMethodShowedHandler.handle(any()) } just Runs

        val navigator = mockk<Navigator<NavigationParams>>(relaxed = true) {
            every { canHandle(any()) } returns true
            every { navigate(any()) } just runs
        }
        val navigationHandler = mockk<PaymentMethodContextNavigationHandler>(relaxed = true) {
            every { getSupportedNavigators(any()) } returns listOf(navigator)
        }
        every { paymentMethodNavigationFactoryRegistry.create(paymentMethodType) } returns navigationHandler

        runTest {
            uiEventFlow.emit(ComposerUiEvent.Navigate(mockk(relaxed = true)))
            val job = launch {
                paymentMethodStarter.start(context, paymentMethodType, sessionIntent, category)
            }
            advanceUntilIdle()
            job.cancel()
        }

        verify { composerRegistry.unregister(paymentMethodType) }
        verify { composerRegistry.register(paymentMethodType, composer) }
        coVerify { paymentMethodShowedHandler.handle(paymentMethodType) }
    }

    @Test
    fun `start should handle UI events and navigate correctly`() {
        val context = mockk<Context>(relaxed = true)
        val paymentMethodType = "testType"
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val category = PrimerPaymentMethodManagerCategory.NATIVE_UI

        val uiEventFlow = MutableSharedFlow<ComposerUiEvent>(replay = 1)
        val composer = mockk<BaseWebRedirectComposer>(relaxed = true) {
            every { uiEvent } returns uiEventFlow
        }
        every { providerFactoryRegistry.create(paymentMethodType, sessionIntent) } returns composer

        val navigationHandler = mockk<PaymentMethodContextNavigationHandler>(relaxed = true)
        every { paymentMethodNavigationFactoryRegistry.create(paymentMethodType) } returns navigationHandler

        runTest {
            uiEventFlow.emit(ComposerUiEvent.Navigate(mockk(relaxed = true)))
            val job = launch {
                paymentMethodStarter.start(context, paymentMethodType, sessionIntent, category)
            }
            advanceUntilIdle()
            job.cancel()
        }
        verify { navigationHandler.getSupportedNavigators(any()) }
    }
}
