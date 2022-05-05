package io.primer.android.components.di

import io.primer.android.components.data.payments.repository.CheckoutModuleDataRepository
import io.primer.android.components.domain.core.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.components.domain.inputs.PaymentInputTypesInteractor
import io.primer.android.components.domain.payments.PaymentTokenizationInteractor
import io.primer.android.components.domain.payments.PaymentsTypesInteractor
import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.components.presentation.HeadlessUniversalCheckoutViewModel
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
        single { PrimerPaymentMethodViewFactory(get()) }
        single { PrimerHeadlessUniversalCheckoutPaymentMethodMapper() }
        single<CheckoutModuleRepository> { CheckoutModuleDataRepository(get()) }

        single {
            PaymentTokenizationInteractor(
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
        factory { HeadlessUniversalCheckoutViewModel(get(), get(), get(), get(), get(), get()) }
    }
}
