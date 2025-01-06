@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.components.di

import io.primer.android.clientSessionActions.di.ActionsContainer
import io.primer.android.clientSessionActions.domain.handlers.CheckoutClientSessionActionsHandler
import io.primer.android.components.DefaultNativeUiManagerHeadlessManagerDelegate
import io.primer.android.components.DefaultPaymentMethodManagerDelegate
import io.primer.android.components.DefaultRawDataManagerDelegate
import io.primer.android.components.PaymentInputTypesInteractor
import io.primer.android.components.PaymentMethodManagerDelegate
import io.primer.android.components.RawDataDelegate
import io.primer.android.components.assets.validation.SdkInitializedRule
import io.primer.android.components.implementation.completion.DefaultCheckoutAdditionalInfoHandler
import io.primer.android.components.implementation.completion.DefaultCheckoutClientSessionActionsHandler
import io.primer.android.components.implementation.completion.DefaultCheckoutErrorHandler
import io.primer.android.components.implementation.completion.DefaultCheckoutSuccessHandler
import io.primer.android.components.implementation.completion.DefaultPaymentMethodShowedHandler
import io.primer.android.components.implementation.completion.DefaultPendingResumeHandler
import io.primer.android.components.implementation.completion.DefaultPollingStartHandler
import io.primer.android.components.implementation.completion.DefaultPostResumeHandler
import io.primer.android.components.implementation.completion.DefaultPostTokenizationHandler
import io.primer.android.components.implementation.completion.DefaultPreTokenizationHandler
import io.primer.android.components.implementation.completion.DefaultPreparationStartHandler
import io.primer.android.components.implementation.completion.HeadlessManualFlowSuccessHandler
import io.primer.android.components.implementation.core.presentation.DefaultPaymentMethodInitializer
import io.primer.android.components.implementation.core.presentation.DefaultPaymentMethodStarter
import io.primer.android.components.implementation.core.presentation.PaymentMethodInitializer
import io.primer.android.components.implementation.core.presentation.PaymentMethodStarter
import io.primer.android.components.implementation.domain.PaymentsTypesInteractor
import io.primer.android.components.implementation.domain.handler.AvailablePaymentMethodsHandler
import io.primer.android.components.implementation.domain.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.components.implementation.presentation.DefaultHeadlessUniversalCheckoutDelegate
import io.primer.android.components.validation.resolvers.PaymentMethodManagerInitValidationRulesResolver
import io.primer.android.components.validation.resolvers.PaymentMethodManagerSessionIntentRulesResolver
import io.primer.android.components.validation.rules.ValidPaymentMethodManagerRule
import io.primer.android.components.validation.rules.ValidPaymentMethodRule
import io.primer.android.components.validation.rules.ValidSessionIntentRule
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.paymentmethods.PrimerRawData
import io.primer.android.payments.core.create.domain.handler.PostTokenizationHandler
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.ManualFlowSuccessHandler
import io.primer.android.payments.core.helpers.PaymentMethodShowedHandler
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.payments.core.helpers.PreparationStartHandler
import io.primer.android.payments.core.resume.domain.handler.PendingResumeHandler
import io.primer.android.payments.core.resume.domain.handler.PostResumeHandler
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob

internal class ComponentsContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    @Suppress("LongMethod")
    override fun registerInitialDependencies() {
        registerFactory {
            SdkInitializedRule(configurationRepository = sdk().resolve())
        }
        registerSingleton {
            PrimerHeadlessUniversalCheckoutPaymentMethodMapper(paymentMethodDescriptorsRepository = sdk().resolve())
        }

        registerSingleton {
            AvailablePaymentMethodsHandler(analyticsRepository = sdk().resolve())
        }

        registerSingleton {
            PaymentsTypesInteractor(
                configurationInteractor = sdk().resolve(ConfigurationCoreContainer.CONFIGURATION_INTERACTOR_DI_KEY),
                paymentMethodModulesInteractor = sdk().resolve(),
                paymentMethodMapper = resolve(),
                fetchCurrencyFormatDataInteractor = sdk().resolve(),
                availablePaymentMethodsHandler = sdk().resolve(),
                primerSettings = sdk().resolve(),
                logReporter = sdk().resolve(),
            )
        }

        registerSingleton<CoroutineScopeProvider> {
            object : CoroutineScopeProvider {
                override val scope: CoroutineScope = CoroutineScope(SupervisorJob())
            }
        }

        registerFactory {
            DefaultHeadlessUniversalCheckoutDelegate(
                paymentsTypesInteractor = resolve(),
                analyticsInteractor = sdk().resolve(),
                globalCacheConfigurationCacheDataSource =
                    sdk().resolve(
                        ConfigurationCoreContainer.GLOBAL_CACHED_CONFIGURATION_DI_KEY,
                    ),
                scopeProvider = resolve(),
            )
        }

        registerFactory<PreTokenizationHandler> {
            DefaultPreTokenizationHandler(analyticsRepository = sdk().resolve(), config = sdk().resolve())
        }

        registerFactory<PostTokenizationHandler> {
            DefaultPostTokenizationHandler(analyticsRepository = sdk().resolve())
        }

        registerFactory<PostResumeHandler> {
            DefaultPostResumeHandler(analyticsRepository = sdk().resolve())
        }

        registerFactory<PendingResumeHandler> {
            DefaultPendingResumeHandler(analyticsRepository = sdk().resolve())
        }

        registerSingleton<ManualFlowSuccessHandler> {
            HeadlessManualFlowSuccessHandler()
        }

        registerFactory<CheckoutSuccessHandler> {
            DefaultCheckoutSuccessHandler(analyticsRepository = sdk().resolve())
        }

        registerSingleton<PreparationStartHandler> {
            DefaultPreparationStartHandler(analyticsRepository = sdk().resolve())
        }

        registerSingleton<PaymentMethodShowedHandler> {
            DefaultPaymentMethodShowedHandler(analyticsRepository = sdk().resolve())
        }

        registerSingleton<PollingStartHandler> {
            DefaultPollingStartHandler()
        }

        registerSingleton<CheckoutErrorHandler> {
            DefaultCheckoutErrorHandler(analyticsRepository = sdk().resolve(), config = sdk().resolve())
        }

        registerSingleton<CheckoutAdditionalInfoHandler> {
            DefaultCheckoutAdditionalInfoHandler(
                analyticsRepository = sdk().resolve(),
                config = sdk().resolve(),
                paymentResultRepository = sdk().resolve(),
            )
        }

        registerFactory<CheckoutClientSessionActionsHandler> {
            DefaultCheckoutClientSessionActionsHandler(
                analyticsRepository = sdk().resolve(),
                checkoutErrorHandler = resolve(),
            )
        }

        registerFactory {
            ValidPaymentMethodManagerRule(paymentMethodManager = resolve())
        }

        registerFactory {
            ValidPaymentMethodRule(paymentMethodRepository = sdk().resolve())
        }

        registerFactory {
            ValidSessionIntentRule(paymentMethodManager = resolve())
        }

        registerFactory {
            PaymentMethodManagerInitValidationRulesResolver(
                sdkInitializedRule = resolve(),
                validPaymentMethodRule = resolve(),
                validPaymentMethodManagerRule = resolve(),
            )
        }

        registerFactory {
            PaymentMethodManagerSessionIntentRulesResolver(validSessionIntentRule = resolve())
        }

        registerFactory<PaymentMethodInitializer> {
            DefaultPaymentMethodInitializer(
                initValidationRulesResolver = resolve(),
                analyticsInteractor = sdk().resolve(),
            )
        }

        registerFactory<PaymentMethodStarter> {
            DefaultPaymentMethodStarter(
                analyticsInteractor = sdk().resolve(),
                composerRegistry = sdk().resolve(),
                providerFactoryRegistry = sdk().resolve(),
                paymentMethodNavigationFactoryRegistry = sdk().resolve(),
                paymentMethodShowedHandler = sdk().resolve(),
            )
        }

        registerFactory<PaymentMethodManagerDelegate> {
            DefaultPaymentMethodManagerDelegate(
                paymentMethodInitializer = resolve(),
                paymentMethodStarter = resolve(),
            )
        }

        registerSingleton {
            PaymentInputTypesInteractor(
                sdk().resolve(),
                sdk().resolve(),
                sdk().resolve(),
            )
        }

        registerFactory {
            DefaultNativeUiManagerHeadlessManagerDelegate(
                actionInteractor = sdk().resolve(ActionsContainer.ACTION_INTERACTOR_DI_KEY),
                sessionIntentRulesResolver = resolve(),
                paymentMethodInitializer = resolve(),
                paymentMethodStarter = resolve(),
                composerRegistry = sdk().resolve(),
                preparationStartHandler = sdk().resolve(),
                headlessScopeProvider = resolve(),
            )
        }

        registerFactory<RawDataDelegate<PrimerRawData>> {
            DefaultRawDataManagerDelegate(
                initValidationRulesResolver = resolve(),
                paymentInputTypesInteractor = resolve(),
                paymentMethodMapper = resolve(),
                analyticsInteractor = sdk().resolve(),
                composerRegistry = sdk().resolve(),
                providerFactoryRegistry = sdk().resolve(),
                paymentMethodNavigationFactoryRegistry = sdk().resolve(),
                actionInteractor = sdk().resolve(ActionsContainer.ACTION_INTERACTOR_DI_KEY),
                logReporter = sdk().resolve(),
                preparationStartHandler = sdk().resolve(),
            )
        }
    }
}
