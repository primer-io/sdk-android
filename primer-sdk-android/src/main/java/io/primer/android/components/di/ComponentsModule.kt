package io.primer.android.components.di

import io.primer.android.components.data.payments.repository.CheckoutModuleDataRepository
import io.primer.android.components.domain.assets.validation.resolvers.AssetManagerInitValidationRulesResolver
import io.primer.android.components.domain.core.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.components.domain.inputs.PaymentInputTypesInteractor
import io.primer.android.components.domain.payments.PaymentRawDataChangedInteractor
import io.primer.android.components.domain.payments.PaymentRawDataTypeValidateInteractor
import io.primer.android.components.domain.payments.PaymentTokenizationInteractor
import io.primer.android.components.domain.payments.PaymentsTypesInteractor
import io.primer.android.components.domain.payments.metadata.PaymentRawDataMetadataRetrieverFactory
import io.primer.android.components.domain.payments.paymentMethods.PaymentRawDataValidationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.resolvers.PaymentMethodManagerInitValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.resolvers.PaymentMethodManagerSessionIntentRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.SdkInitializedRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.ValidPaymentMethodManagerRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.ValidPaymentMethodRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.ValidSessionIntentRule
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidatorFactory
import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.components.presentation.DefaultHeadlessUniversalCheckoutDelegate
import io.primer.android.components.presentation.assets.DefaultAssetsHeadlessDelegate
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.raw.DefaultRawDataManagerDelegate
import io.primer.android.components.ui.navigation.Navigator
import io.primer.android.components.ui.views.PrimerPaymentMethodViewFactory
import io.primer.android.logging.DefaultLogger
import io.primer.android.logging.Logger
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val COMPONENTS_HANDLER_LOGGER_NAME = "PRIMER_HEADLESS_CHECKOUT"

internal val componentsModule = {
    module {
        factory<Logger>(named(COMPONENTS_HANDLER_LOGGER_NAME)) {
            DefaultLogger(
                COMPONENTS_HANDLER_LOGGER_NAME
            )
        }
        single { Navigator(get()) }
        single { PrimerPaymentMethodViewFactory(get(), get()) }
        single { PrimerHeadlessUniversalCheckoutPaymentMethodMapper(get()) }
        single<CheckoutModuleRepository> { CheckoutModuleDataRepository(get()) }

        single {
            PaymentTokenizationInteractor(
                get(),
                get(),
            )
        }
        single {
            PaymentsTypesInteractor(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(named(COMPONENTS_HANDLER_LOGGER_NAME))
            )
        }
        single {
            PaymentInputTypesInteractor(
                get(),
                get(),
                get(named(COMPONENTS_HANDLER_LOGGER_NAME))
            )
        }
        single {
            PaymentRawDataTypeValidateInteractor(
                get(),
                get(),
            )
        }
        factory {
            DefaultHeadlessUniversalCheckoutDelegate(
                get(),
                get()
            )
        }

        factory {
            PaymentInputDataValidatorFactory(
                get(),
                get(),
            )
        }

        factory {
            PaymentRawDataMetadataRetrieverFactory()
        }

        factory {
            PaymentRawDataChangedInteractor(
                get(),
                get(),
                get(),
            )
        }

        factory {
            PaymentRawDataValidationInteractor(
                get(),
            )
        }

        factory {
            DefaultAssetsHeadlessDelegate(
                get(),
                get(),
                get()
            )
        }

        factory {
            ValidPaymentMethodManagerRule(get())
        }

        factory {
            ValidPaymentMethodRule(get())
        }
        factory {
            SdkInitializedRule(get())
        }

        factory {
            ValidSessionIntentRule(get())
        }

        factory {
            PaymentMethodManagerInitValidationRulesResolver(get(), get(), get())
        }

        factory {
            PaymentMethodManagerSessionIntentRulesResolver(get())
        }

        factory {
            AssetManagerInitValidationRulesResolver(get())
        }

        single {
            DefaultRawDataManagerDelegate(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }

        single {
            DefaultHeadlessManagerDelegate(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }
    }
}
