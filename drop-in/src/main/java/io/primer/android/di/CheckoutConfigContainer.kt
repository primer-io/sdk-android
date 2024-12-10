package io.primer.android.di

import io.primer.android.Primer
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.clientSessionActions.di.ActionsContainer
import io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager
import io.primer.android.components.ui.views.PrimerPaymentMethodViewFactory
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.configuration.domain.BasicOrderInfoInteractor
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.currencyformat.domain.FormatAmountToCurrencyInteractor
import io.primer.android.payment.billing.BillingAddressValidator
import io.primer.android.payment.billing.DefaultBillingAddressValidator
import io.primer.android.paymentMethods.core.DefaultPaymentMethodMapping
import io.primer.android.paymentMethods.core.PaymentMethodMapping
import io.primer.android.paymentMethods.core.PrimerHeadlessSdkInitInteractor
import io.primer.android.paymentMethods.core.data.repository.DefaultPrimerHeadlessRepository
import io.primer.android.paymentMethods.core.domain.PrimerEventsInteractor
import io.primer.android.paymentMethods.core.domain.repository.PrimerHeadlessRepository
import io.primer.android.paymentMethods.core.ui.assets.AssetsManager
import io.primer.android.paymentMethods.core.ui.assets.DefaultPrimerAssetsManager
import io.primer.android.paymentMethods.core.ui.descriptors.PrimerDropInPaymentMethodDescriptorRegistry
import io.primer.android.payments.core.helpers.CheckoutExitHandler
import io.primer.android.payments.core.helpers.ManualFlowSuccessHandler
import io.primer.android.presentation.base.BaseViewModelFactory
import io.primer.android.surcharge.domain.SurchargeInteractor
import io.primer.android.ui.utils.DefaultCheckoutExitHandler
import io.primer.android.ui.utils.DropInManualFlowSuccessHandler
import io.primer.android.viewmodel.PrimerViewModelFactory

internal class CheckoutConfigContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { sdk().resolve<PrimerConfig>().settings.uiOptions.theme }

        registerFactory {
            BaseViewModelFactory(sdk().resolve())
        }

        registerFactory {
            PrimerDropInPaymentMethodDescriptorRegistry()
        }

        registerSingleton<CheckoutExitHandler> {
            DefaultCheckoutExitHandler(onExit = {
                Primer.current.listener?.onDismissed()
            })
        }

        registerSingleton<BillingAddressValidator> { DefaultBillingAddressValidator() }

        registerSingleton<PaymentMethodMapping> {
            DefaultPaymentMethodMapping(
                config = sdk().resolve(),
                brandRegistry = sdk().resolve()
            )
        }

        registerSingleton<PrimerHeadlessRepository> {
            DefaultPrimerHeadlessRepository(
                context = sdk().resolve(),
                config = sdk().resolve()
            )
        }

        registerFactory {
            PrimerHeadlessSdkInitInteractor(
                headlessRepository = resolve()
            )
        }

        registerFactory {
            PrimerEventsInteractor(
                headlessRepository = resolve<PrimerHeadlessRepository>(),
                exitHandler = resolve<CheckoutExitHandler>(),
                config = sdk().resolve()
            )
        }

        registerFactory<AssetsManager> {
            DefaultPrimerAssetsManager(
                PrimerHeadlessUniversalCheckoutAssetsManager.Companion
            )
        }

        registerSingleton {
            FormatAmountToCurrencyInteractor(
                currencyFormatRepository = sdk().resolve(),
                settings = sdk().resolve()
            )
        }

        registerSingleton {
            BasicOrderInfoInteractor(
                sdk().resolve()
            )
        }

        registerSingleton {
            SurchargeInteractor(
                sdk().resolve()
            )
        }

        registerSingleton<ManualFlowSuccessHandler> {
            DropInManualFlowSuccessHandler(primerHeadlessRepository = sdk().resolve())
        }

        registerFactory {
            PrimerViewModelFactory(
                configurationInteractor = sdk().resolve(
                    dependencyName = ConfigurationCoreContainer.CONFIGURATION_INTERACTOR_DI_KEY
                ),
                paymentMethodsImplementationInteractor = sdk().resolve(),
                analyticsInteractor = sdk().resolve(),
                headlessSdkInitInteractor = resolve(),
                eventsInteractor = resolve(),
                actionInteractor = sdk().resolve(dependencyName = ActionsContainer.ACTION_INTERACTOR_DI_KEY),
                config = sdk().resolve(),
                amountToCurrencyInteractor = resolve(),
                basicOrderInfoInteractor = resolve(),
                surchargeInteractor = resolve(),
                billingAddressValidator = sdk().resolve(),
                registry = resolve(),
                errorMapperRegistry = sdk().resolve(),
                checkoutErrorHandler = sdk().resolve(),
                paymentMethodMapping = resolve(),
                pollingStartHandler = sdk().resolve()
            )
        }

        registerSingleton {
            PrimerPaymentMethodViewFactory(
                config = sdk().resolve(),
                context = sdk().resolve(),
                assetsManager = resolve()
            )
        }
    }
}
