package io.primer.android.components

import android.content.Context
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodBinData
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.paymentmethods.core.composer.RawDataPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.registry.PaymentMethodComposerRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData
import io.primer.android.payments.core.helpers.PreparationStartHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class DefaultRawDataManagerDelegateTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val composer: RawDataPaymentMethodComponent<PrimerCollectableData> = mockk(relaxed = true)
    private val listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener = mockk(relaxed = true)
    private val providerFactoryRegistry: PaymentMethodProviderFactoryRegistry = mockk(relaxed = true)
    private val composerRegistry: PaymentMethodComposerRegistry = mockk(relaxed = true)

    private lateinit var delegate: DefaultRawDataManagerDelegate

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { providerFactoryRegistry.create(any(), any()) } returns composer
        every { composer.binDataFlow } returns null
        every { composer.metadataFlow } returns emptyFlow()
        every { composer.metadataStateFlow } returns emptyFlow()
        every { composer.componentInputValidations } returns emptyFlow()
        every { composer.start(any(), any()) } just Runs

        delegate = DefaultRawDataManagerDelegate(
            initValidationRulesResolver = mockk(relaxed = true),
            paymentInputTypesInteractor = mockk(relaxed = true),
            paymentMethodMapper = mockk(relaxed = true),
            analyticsInteractor = mockk(relaxed = true),
            composerRegistry = composerRegistry,
            providerFactoryRegistry = providerFactoryRegistry,
            paymentMethodNavigationFactoryRegistry = mockk<PaymentMethodNavigationFactoryRegistry>(relaxed = true),
            actionInteractor = mockk(relaxed = true),
            logReporter = mockk<LogReporter>(relaxed = true),
            preparationStartHandler = mockk<PreparationStartHandler>(relaxed = true),
        )
        delegate.setListener(listener)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `start should forward binData emissions to listener onBinDataAvailable`() = runTest {
        val binDataFlow = MutableSharedFlow<PrimerPaymentMethodBinData>()
        every { composer.binDataFlow } returns binDataFlow

        val context = mockk<Context>(relaxed = true)
        delegate.start(context, "PAYMENT_CARD", PrimerSessionIntent.CHECKOUT)
        advanceUntilIdle()

        val binData = mockk<PrimerPaymentMethodBinData>()
        binDataFlow.emit(binData)
        advanceUntilIdle()

        verify { listener.onBinDataAvailable(binData) }
    }

    @Test
    fun `start should not crash when binDataFlow is null`() = runTest {
        every { composer.binDataFlow } returns null

        val context = mockk<Context>(relaxed = true)
        delegate.start(context, "PAYMENT_CARD", PrimerSessionIntent.CHECKOUT)
        advanceUntilIdle()

        verify(exactly = 0) { listener.onBinDataAvailable(any()) }
    }
}
