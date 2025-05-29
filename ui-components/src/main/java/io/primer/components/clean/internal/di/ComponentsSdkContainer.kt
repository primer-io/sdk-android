package io.primer.components.clean.internal.di

import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.components.clean.internal.data.mappers.PaymentMethodMapper
import io.primer.components.clean.internal.data.mappers.PaymentMethodMapperImpl
import io.primer.components.clean.internal.data.repositories.HeadlessRepositoryImpl
import io.primer.components.clean.internal.domain.repositories.HeadlessRepository
import io.primer.components.clean.internal.domain.usecases.GetAvailablePaymentMethodsUseCase
import io.primer.components.clean.internal.domain.usecases.ProcessCardPaymentUseCase
import io.primer.components.clean.internal.domain.usecases.ValidateCardUseCase

/**
 * Components SDK Container that aggregates all component-related containers
 */
internal class ComponentsSdkContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {

        registerSingleton<PaymentMethodMapper> {
            PaymentMethodMapperImpl()
        }

        registerSingleton<HeadlessRepository> {
            HeadlessRepositoryImpl(PrimerHeadlessUniversalCheckout.current)
        }

        registerSingleton<GetAvailablePaymentMethodsUseCase> {
            GetAvailablePaymentMethodsUseCase(sdk().resolve())
        }

        registerSingleton<ProcessCardPaymentUseCase> {
            ProcessCardPaymentUseCase(sdk().resolve())
        }

        registerSingleton<ValidateCardUseCase> {
            ValidateCardUseCase(sdk().resolve())
        }
    }
}
