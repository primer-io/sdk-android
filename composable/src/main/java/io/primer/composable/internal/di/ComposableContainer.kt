package io.primer.composable.internal.di

import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.composable.internal.data.mappers.PaymentMethodMapper
import io.primer.composable.internal.data.mappers.PaymentMethodMapperImpl
import io.primer.composable.internal.data.repositories.HeadlessRepositoryImpl
import io.primer.composable.internal.domain.repositories.HeadlessRepository
import io.primer.composable.internal.domain.usecases.GetAvailablePaymentMethodsUseCase
import io.primer.composable.internal.presentation.checkout.CheckoutNavigator

internal class ComposableContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {

        registerSingleton<PaymentMethodMapper> {
            PaymentMethodMapperImpl()
        }

        registerSingleton<HeadlessRepository> {
            HeadlessRepositoryImpl(PrimerHeadlessUniversalCheckout.current)
        }

        registerSingleton {
            GetAvailablePaymentMethodsUseCase(resolve(), resolve())
        }

        registerSingleton {
            CheckoutNavigator()
        }
    }
}
