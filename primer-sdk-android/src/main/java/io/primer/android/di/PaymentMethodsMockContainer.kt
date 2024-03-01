package io.primer.android.di

import io.primer.android.components.presentation.mock.delegate.MockConfigurationDelegate
import io.primer.android.data.mock.datasource.RemoteFinalizeMockedFlowDataSource
import io.primer.android.data.mock.repository.MockDataConfigurationRepository
import io.primer.android.domain.mock.FinaliseMockedFlowInteractor
import io.primer.android.domain.mock.MockConfigurationInteractor
import io.primer.android.domain.mock.repository.MockConfigurationRepository
import io.primer.android.presentation.mock.PaymentMethodMockViewModelFactory

internal class PaymentMethodsMockContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { RemoteFinalizeMockedFlowDataSource(sdk.resolve()) }

        registerSingleton<MockConfigurationRepository> {
            MockDataConfigurationRepository(
                sdk.resolve(),
                resolve()
            )
        }

        registerSingleton { MockConfigurationInteractor(resolve()) }

        registerSingleton { MockConfigurationDelegate(resolve()) }

        registerSingleton { FinaliseMockedFlowInteractor(resolve()) }

        registerFactory {
            PaymentMethodMockViewModelFactory(
                resolve(),
                sdk.resolve()
            )
        }
    }
}
