package io.primer.android.components.di

import io.primer.android.components.data.payments.repository.CheckoutModuleDataRepository
import io.primer.android.components.domain.core.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.components.domain.inputs.PaymentInputTypesInteractor
import io.primer.android.components.domain.payments.PaymentInputDataChangedInteractor
import io.primer.android.components.domain.payments.PaymentInputDataTypeValidateInteractor
import io.primer.android.components.domain.payments.PaymentTokenizationInteractor
import io.primer.android.components.domain.payments.PaymentsTypesInteractor
import io.primer.android.components.domain.payments.metadata.PaymentRawDataMetadataRetrieverFactory
import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.components.domain.payments.validation.PaymentInputDataValidatorFactory
import io.primer.android.components.presentation.DefaultHeadlessUniversalCheckoutDelegate
import io.primer.android.components.presentation.DefaultRawDataDelegate
import io.primer.android.components.ui.navigation.Navigator
import io.primer.android.components.ui.views.PrimerPaymentMethodViewFactory
import io.primer.android.di.RETAIL_OUTLET_SCOPE
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
        single { PrimerHeadlessUniversalCheckoutPaymentMethodMapper() }
        single<CheckoutModuleRepository> { CheckoutModuleDataRepository(get()) }

        single {
            PaymentTokenizationInteractor(
                get(),
                get(),
                get(),
                get(),
                get(named(COMPONENTS_HANDLER_LOGGER_NAME))
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
            PaymentInputDataTypeValidateInteractor(
                get(),
                get(),
                get(),
            )
        }
        factory {
            DefaultHeadlessUniversalCheckoutDelegate(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                getScope(RETAIL_OUTLET_SCOPE).get(),
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }

        factory {
            PaymentInputDataValidatorFactory(
                get(),
            )
        }

        factory {
            PaymentRawDataMetadataRetrieverFactory()
        }

        factory {
            PaymentInputDataChangedInteractor(
                get(),
                get(),
                get(),
            )
        }

        factory {
            DefaultRawDataDelegate(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                getScope(RETAIL_OUTLET_SCOPE).get(),
                get(),
                get(),
                get()
            )
        }
    }
}
