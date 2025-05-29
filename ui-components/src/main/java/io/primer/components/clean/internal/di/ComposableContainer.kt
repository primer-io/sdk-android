package io.primer.components.clean.internal.di

import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.components.clean.internal.data.mappers.PaymentMethodMapper
import io.primer.components.clean.internal.data.mappers.PaymentMethodMapperImpl
import io.primer.components.clean.internal.data.repositories.HeadlessRepositoryImpl
import io.primer.components.clean.internal.domain.repositories.HeadlessRepository
import io.primer.components.clean.internal.domain.usecases.GetAvailablePaymentMethodsUseCase

internal class ComposableContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {

        registerSingleton<PaymentMethodMapper> {
            PaymentMethodMapperImpl()
        }

        registerSingleton<HeadlessRepository> {
            HeadlessRepositoryImpl(resolve())
        }

        registerSingleton {
            GetAvailablePaymentMethodsUseCase(resolve(), resolve())
        }
    }
}
