package io.primer.android.clientSessionActions.di

import io.primer.android.clientSessionActions.data.datasource.RemoteActionDataSource
import io.primer.android.clientSessionActions.data.repository.ActionDataRepository
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.DefaultActionInteractor
import io.primer.android.clientSessionActions.domain.repository.ActionRepository
import io.primer.android.clientSessionActions.domain.validator.ActionUpdateFilter
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.BaseDataProvider

class ActionsContainer(
    private val sdk: () -> SdkContainer,
) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton {
            RemoteActionDataSource(
                httpClient = sdk().resolve(),
                apiVersion = sdk().resolve<BaseDataProvider<PrimerApiVersion>>()::provide,
            )
        }

        registerSingleton<ActionRepository> {
            ActionDataRepository(
                configurationDataSource =
                    sdk().resolve(
                        ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY,
                    ),
                remoteActionDataSource = resolve(),
                globalCacheDataSource = sdk().resolve(ConfigurationCoreContainer.GLOBAL_CACHED_CONFIGURATION_DI_KEY),
                clientTokenProvider = sdk().resolve(ConfigurationCoreContainer.CLIENT_TOKEN_PROVIDER_DI_KEY),
            )
        }

        registerFactory { ActionUpdateFilter(configurationRepository = sdk().resolve(), config = sdk().resolve()) }

        registerSingleton<ActionInteractor>(
            ACTION_INTERACTOR_DI_KEY,
        ) {
            DefaultActionInteractor(
                actionRepository = resolve(),
                configurationRepository = sdk().resolve(),
                actionUpdateFilter = resolve(),
                errorEventResolver = sdk().resolve(),
                clientSessionActionsHandler = sdk().resolve(),
                ignoreErrors = false,
            )
        }
        registerSingleton<ActionInteractor>(
            ACTION_INTERACTOR_IGNORE_ERRORS_DI_KEY,
        ) {
            DefaultActionInteractor(
                actionRepository = resolve(),
                configurationRepository = sdk().resolve(),
                actionUpdateFilter = resolve(),
                errorEventResolver = sdk().resolve(),
                clientSessionActionsHandler = sdk().resolve(),
                ignoreErrors = true,
            )
        }
    }

    companion object {
        const val ACTION_INTERACTOR_DI_KEY = "ACTION_INTERACTOR"
        const val ACTION_INTERACTOR_IGNORE_ERRORS_DI_KEY = "ACTION_INTERACTOR_IGNORE_ERRORS"
    }
}
