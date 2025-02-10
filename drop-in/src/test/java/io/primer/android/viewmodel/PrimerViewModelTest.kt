package io.primer.android.viewmodel

import com.jraska.livedata.test
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.components.assets.displayMetadata.PaymentMethodsImplementationInteractor
import io.primer.android.configuration.domain.BasicOrderInfoInteractor
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.currencyformat.domain.FormatAmountToCurrencyInteractor
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.payment.billing.BillingAddressValidator
import io.primer.android.paymentMethods.core.PaymentMethodMapping
import io.primer.android.paymentMethods.core.PrimerHeadlessSdkCleanupInteractor
import io.primer.android.paymentMethods.core.PrimerHeadlessSdkInitInteractor
import io.primer.android.paymentMethods.core.domain.PrimerEventsInteractor
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.PrimerDropInPaymentMethodDescriptorRegistry
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.surcharge.domain.SurchargeInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class PrimerViewModelTest {
    @RelaxedMockK
    lateinit var configurationInteractor: ConfigurationInteractor

    @RelaxedMockK
    lateinit var paymentMethodsImplementationInteractor: PaymentMethodsImplementationInteractor

    @RelaxedMockK
    lateinit var analyticsInteractor: AnalyticsInteractor

    @RelaxedMockK
    lateinit var headlessSdkInitInteractor: PrimerHeadlessSdkInitInteractor

    @RelaxedMockK
    lateinit var headlessSdkCleanupInteractor: PrimerHeadlessSdkCleanupInteractor

    @RelaxedMockK
    lateinit var eventsInteractor: PrimerEventsInteractor

    @RelaxedMockK
    lateinit var actionInteractor: ActionInteractor

    @RelaxedMockK
    lateinit var amountToCurrencyInteractor: FormatAmountToCurrencyInteractor

    @RelaxedMockK
    lateinit var config: PrimerConfig

    @RelaxedMockK
    lateinit var billingAddressValidator: BillingAddressValidator

    @RelaxedMockK
    lateinit var basicOrderInfoInteractor: BasicOrderInfoInteractor

    @RelaxedMockK
    lateinit var surchargeInteractor: SurchargeInteractor

    @RelaxedMockK
    lateinit var registry: PrimerDropInPaymentMethodDescriptorRegistry

    @RelaxedMockK
    lateinit var paymentMethodMapping: PaymentMethodMapping

    @RelaxedMockK
    lateinit var errorMapperRegistry: ErrorMapperRegistry

    @RelaxedMockK
    lateinit var checkoutErrorHandler: CheckoutErrorHandler

    @RelaxedMockK
    lateinit var pollingStartHandler: PollingStartHandler

    private lateinit var viewModel: PrimerViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel =
            PrimerViewModel(
                configurationInteractor = configurationInteractor,
                paymentMethodsImplementationInteractor = paymentMethodsImplementationInteractor,
                analyticsInteractor = analyticsInteractor,
                headlessSdkInitInteractor = headlessSdkInitInteractor,
                headlessSdkCleanupInteractor = headlessSdkCleanupInteractor,
                eventsInteractor = eventsInteractor,
                actionInteractor = actionInteractor,
                amountToCurrencyInteractor = amountToCurrencyInteractor,
                config = config,
                billingAddressValidator = billingAddressValidator,
                basicOrderInfoInteractor = basicOrderInfoInteractor,
                surchargeInteractor = surchargeInteractor,
                registry = registry,
                paymentMethodMapping = paymentMethodMapping,
                errorMapperRegistry = errorMapperRegistry,
                checkoutErrorHandler = checkoutErrorHandler,
                pollingStartHandler = pollingStartHandler,
            )
    }

    @Test
    fun `setSelectedPaymentMethodId() should clear selected payment method`() {
        val paymentMethodDropInDescriptor = mockk<PaymentMethodDropInDescriptor>(relaxed = true)
        val observer = viewModel.selectedPaymentMethod.test()
        viewModel.selectPaymentMethod(paymentMethodDropInDescriptor)
        viewModel.setSelectedPaymentMethodId("123")

        assertEquals(listOf(null, paymentMethodDropInDescriptor, null), observer.valueHistory())
    }
}
