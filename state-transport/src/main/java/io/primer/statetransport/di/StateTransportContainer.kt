package io.primer.statetransport.di

import io.primer.android.analytics.utils.Constants
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.BaseDataProvider
import io.primer.statetransport.data.datasource.RemoteInstructionsDataSource
import io.primer.statetransport.data.datasource.RemotePayDataSource
import io.primer.statetransport.data.repository.DefaultStateTransportRepository
import io.primer.statetransport.domain.interactor.PaymentFlowInteractor
import io.primer.statetransport.domain.repository.StateTransportRepository

class StateTransportContainer(
    private val sdk: () -> SdkContainer,
) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerFactory {
            RemotePayDataSource(
                httpClient = sdk().resolve(),
                apiVersion = sdk().resolve<BaseDataProvider<PrimerApiVersion>>()::provide,
            )
        }

        registerFactory {
            RemoteInstructionsDataSource(
                httpClient = sdk().resolve(),
                apiVersion = sdk().resolve<BaseDataProvider<PrimerApiVersion>>()::provide,
            )
        }

        registerFactory<StateTransportRepository> {
            DefaultStateTransportRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                remoteStartDataSource = resolve(),
                remoteInstructionsDataSource = resolve(),
                applicationIdProvider = sdk().resolve(Constants.APPLICATION_ID_PROVIDER_DI_KEY),
            )
        }

        registerFactory {
            PaymentFlowInteractor(
                repository = resolve(),
                configurationRepository = sdk().resolve(),
            )
        }
    }
}
