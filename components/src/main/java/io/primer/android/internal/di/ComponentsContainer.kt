package io.primer.android.internal.di

import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.internal.data.mappers.PaymentMethodMapper
import io.primer.android.internal.data.mappers.PaymentMethodMapperImpl
import io.primer.android.internal.data.repositories.HeadlessRepositoryImpl
import io.primer.android.internal.data.repositories.RawDataManagerRepositoryImpl
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import io.primer.android.internal.domain.usecase.CardFieldsUseCase
import io.primer.android.internal.domain.usecase.CardNetworkUseCase
import io.primer.android.internal.domain.usecase.GetAvailablePaymentMethodsUseCase
import io.primer.android.internal.domain.usecase.InitCardManagerUseCase
import io.primer.android.internal.domain.usecase.SubmitCardPaymentUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.screens.card.CardFormViewModel
import io.primer.android.internal.presentation.screens.country.SelectCountryViewModel
import io.primer.android.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionViewModel
import io.primer.android.scope.PrimerCardFormScope
import io.primer.android.scope.PrimerPaymentMethodSelectionScope
import io.primer.android.scope.PrimerSelectCountryScope
import io.primer.android.ui.core.configuration.domain.model.BasicOrderInfoInteractor
import io.primer.android.ui.core.data.repository.CountriesDataRepository
import io.primer.android.ui.core.payment.domain.interactor.SurchargeInteractor

internal class ComponentsContainer(
    @Suppress(
        "UNUSED_PARAMETER",
    ) private val sdk: () -> SdkContainer,
) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<PaymentMethodMapper> {
            PaymentMethodMapperImpl()
        }

        registerSingleton<HeadlessRepository> {
            HeadlessRepositoryImpl(PrimerHeadlessUniversalCheckout.current)
        }

        registerSingleton {
            SurchargeInteractor(sdk().resolve())
        }

        registerSingleton {
            BasicOrderInfoInteractor(sdk().resolve())
        }

        registerSingleton<RawDataManagerRepository> {
            RawDataManagerRepositoryImpl()
        }

        registerSingleton {
            GetAvailablePaymentMethodsUseCase()
        }

        registerSingleton {
            CardFieldsUseCase()
        }

        registerSingleton {
            CardNetworkUseCase()
        }

        registerSingleton {
            SubmitCardPaymentUseCase()
        }

        registerSingleton {
            InitCardManagerUseCase()
        }

        registerSingleton {
            CheckoutNavigator()
        }

        registerSingleton {
            CountriesDataRepository(sdk().resolve())
        }

        registerSingleton<PrimerCardFormScope> {
            CardFormViewModel()
        }

        registerSingleton<PrimerPaymentMethodSelectionScope> {
            PaymentMethodSelectionViewModel()
        }

        registerSingleton<PrimerSelectCountryScope> {
            SelectCountryViewModel()
        }
    }
}
