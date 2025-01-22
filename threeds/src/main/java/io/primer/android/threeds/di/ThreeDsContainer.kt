package io.primer.android.threeds.di

import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.threeds.data.datasource.Remote3DSAuthDataSource
import io.primer.android.threeds.data.error.ThreeDsErrorMapper
import io.primer.android.threeds.data.repository.NetceteraThreeDsServiceRepository
import io.primer.android.threeds.data.repository.ThreeDsAppUrlDataRepository
import io.primer.android.threeds.data.repository.ThreeDsConfigurationDataRepository
import io.primer.android.threeds.data.repository.ThreeDsDataRepository
import io.primer.android.threeds.domain.interactor.DefaultThreeDsInteractor
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor
import io.primer.android.threeds.domain.repository.ThreeDsAppUrlRepository
import io.primer.android.threeds.domain.repository.ThreeDsConfigurationRepository
import io.primer.android.threeds.domain.repository.ThreeDsRepository
import io.primer.android.threeds.domain.repository.ThreeDsServiceRepository
import io.primer.android.threeds.helpers.ThreeDsLibraryVersionValidator
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator
import io.primer.android.threeds.presentation.ThreeDsViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal class ThreeDsContainer(
    private val sdk: SdkContainer,
) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton { ThreeDsSdkClassValidator() }

        registerSingleton { ThreeDsLibraryVersionValidator(configurationRepository = sdk.resolve()) }

        registerSingleton {
            Remote3DSAuthDataSource(
                httpClient = sdk.resolve(),
                apiVersion = sdk.resolve<BaseDataProvider<PrimerApiVersion>>()::provide,
            )
        }

        registerSingleton<ThreeDsRepository> {
            ThreeDsDataRepository(
                dataSource = resolve(),
                configurationDataSource = sdk.resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
            )
        }

        registerFactory<ThreeDsServiceRepository> {
            NetceteraThreeDsServiceRepository(
                context = sdk.resolve(),
            )
        }

        registerFactory<ThreeDsConfigurationRepository> {
            ThreeDsConfigurationDataRepository(
                configurationDataSource = sdk.resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
            )
        }

        registerFactory<ThreeDsAppUrlRepository> { ThreeDsAppUrlDataRepository(settings = sdk.resolve()) }

        registerFactory<ThreeDsInteractor> {
            DefaultThreeDsInteractor(
                threeDsSdkClassValidator = resolve(),
                threeDsLibraryVersionValidator = resolve(),
                threeDsServiceRepository = resolve(),
                threeDsRepository = resolve(),
                tokenizedPaymentMethodRepository = sdk.resolve(),
                threeDsAppUrlRepository = resolve(),
                threeDsConfigurationRepository = resolve(),
                errorMapperRegistry = sdk.resolve(),
                analyticsRepository = sdk.resolve(),
                logReporter = sdk.resolve(),
            )
        }

        registerFactory {
            ThreeDsViewModelFactory(
                threeDsInteractor = resolve(),
                analyticsInteractor = sdk.resolve(),
                settings = sdk.resolve(),
            )
        }

        sdk.resolve<ErrorMapperRegistry>().register(ThreeDsErrorMapper())
    }
}
