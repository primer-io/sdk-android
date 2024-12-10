package io.primer.android.configuration.mock.di

import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.configuration.mock.data.datasource.RemoteFinalizeMockedFlowDataSource
import io.primer.android.configuration.mock.data.repository.MockDataConfigurationRepository
import io.primer.android.configuration.mock.domain.FinaliseMockedFlowInteractor
import io.primer.android.configuration.mock.domain.MockConfigurationInteractor
import io.primer.android.configuration.mock.domain.repository.MockConfigurationRepository
import io.primer.android.configuration.mock.presentation.MockConfigurationDelegate
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer

class MockContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton { RemoteFinalizeMockedFlowDataSource(sdk().resolve()) }

        registerSingleton<MockConfigurationRepository> {
            MockDataConfigurationRepository(
                sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                resolve()
            )
        }

        registerSingleton { MockConfigurationInteractor(resolve()) }

        registerSingleton { MockConfigurationDelegate(resolve()) }

        registerSingleton { FinaliseMockedFlowInteractor(resolve()) }
    }
}
