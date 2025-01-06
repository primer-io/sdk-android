package io.primer.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.components.assets.displayMetadata.PaymentMethodsImplementationInteractor
import io.primer.android.configuration.domain.BasicOrderInfoInteractor
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.currencyformat.domain.FormatAmountToCurrencyInteractor
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.payment.billing.BillingAddressValidator
import io.primer.android.paymentMethods.core.PaymentMethodMapping
import io.primer.android.paymentMethods.core.PrimerHeadlessSdkInitInteractor
import io.primer.android.paymentMethods.core.domain.PrimerEventsInteractor
import io.primer.android.paymentMethods.core.ui.descriptors.PrimerDropInPaymentMethodDescriptorRegistry
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.surcharge.domain.SurchargeInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("LongParameterList")
internal class PrimerViewModelFactory(
    private val configurationInteractor: ConfigurationInteractor,
    private val paymentMethodsImplementationInteractor: PaymentMethodsImplementationInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val headlessSdkInitInteractor: PrimerHeadlessSdkInitInteractor,
    private val eventsInteractor: PrimerEventsInteractor,
    private val actionInteractor: ActionInteractor,
    private val amountToCurrencyInteractor: FormatAmountToCurrencyInteractor,
    private val basicOrderInfoInteractor: BasicOrderInfoInteractor,
    private val surchargeInteractor: SurchargeInteractor,
    private val config: PrimerConfig,
    private val registry: PrimerDropInPaymentMethodDescriptorRegistry,
    private val errorMapperRegistry: ErrorMapperRegistry,
    private val checkoutErrorHandler: CheckoutErrorHandler,
    private val paymentMethodMapping: PaymentMethodMapping,
    private val billingAddressValidator: BillingAddressValidator,
    private val pollingStartHandler: PollingStartHandler,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        return PrimerViewModel(
            configurationInteractor = configurationInteractor,
            paymentMethodsImplementationInteractor = paymentMethodsImplementationInteractor,
            analyticsInteractor = analyticsInteractor,
            headlessSdkInitInteractor = headlessSdkInitInteractor,
            eventsInteractor = eventsInteractor,
            actionInteractor = actionInteractor,
            amountToCurrencyInteractor = amountToCurrencyInteractor,
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            surchargeInteractor = surchargeInteractor,
            config = config,
            billingAddressValidator = billingAddressValidator,
            registry = registry,
            errorMapperRegistry = errorMapperRegistry,
            checkoutErrorHandler = checkoutErrorHandler,
            paymentMethodMapping = paymentMethodMapping,
            pollingStartHandler = pollingStartHandler,
        ) as T
    }
}
