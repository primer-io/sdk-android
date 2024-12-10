package io.primer.android.processor3ds.di

import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.payments.di.PaymentsContainer
import io.primer.android.processor3ds.presentation.Processor3DSViewModelFactory

internal class Processor3dsContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerFactory {
            Processor3DSViewModelFactory(
                pollingInteractor = sdk.resolve(PaymentsContainer.POLLING_INTERACTOR_DI_KEY),
                analyticsInteractor = sdk.resolve()
            )
        }
    }
}
