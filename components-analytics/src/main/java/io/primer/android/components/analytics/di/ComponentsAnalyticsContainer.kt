package io.primer.android.components.analytics.di

import android.content.Context
import io.primer.android.analytics.di.AnalyticsContainer
import io.primer.android.analytics.infrastructure.datasource.connectivity.ConnectivityProvider
import io.primer.android.components.analytics.data.model.IntegrationType
import io.primer.android.components.analytics.data.model.SessionMetadata
import io.primer.android.components.analytics.data.provider.AppMetadataCollector
import io.primer.android.components.analytics.data.provider.DeviceInfoCollector
import io.primer.android.components.analytics.data.provider.DeviceInfoProvider
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepositoryImpl
import io.primer.android.components.analytics.data.repository.ComponentsLoggingRepository
import io.primer.android.components.analytics.data.repository.ComponentsLoggingRepositoryImpl
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.logging.internal.LogReporter

class ComponentsAnalyticsContainer(
    private val sdk: () -> SdkContainer,
    private val integrationType: IntegrationType,
) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<ComponentsEventsRepository> {
            ComponentsEventsRepositoryImpl(
                logReporter = sdk().resolve<LogReporter>(),
                checkoutSessionIdProvider = sdk().resolve(
                    AnalyticsContainer.CHECKOUT_SESSION_ID_PROVIDER_DI_KEY,
                ),
                configurationDataSource = sdk().resolve(
                    ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY,
                ),
                primerConfig = sdk().resolve(),
                httpClient = sdk().resolve(),
                deviceInfoProvider = DeviceInfoProvider(context = sdk().resolve()),
            )
        }

        registerSingleton<ComponentsLoggingRepository> {
            val context = sdk().resolve<Context>()
            val configurationDataSource = sdk().resolve<BaseCacheDataSource<ConfigurationData, ConfigurationData>>(
                ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY,
            )

            ComponentsLoggingRepositoryImpl(
                context = context,
                logReporter = sdk().resolve<LogReporter>(),
                checkoutSessionIdProvider = sdk().resolve(
                    AnalyticsContainer.CHECKOUT_SESSION_ID_PROVIDER_DI_KEY,
                ),
                configurationDataSource = configurationDataSource,
                primerConfig = sdk().resolve(),
                httpClient = sdk().resolve(),
                deviceInfoCollector = DeviceInfoCollector(
                    connectivityProvider = ConnectivityProvider.createProvider(context),
                ),
                appMetadataCollector = AppMetadataCollector(context),
                uncaughtHandlerDataSource = sdk().resolve(
                    AnalyticsContainer.UNCAUGHT_HANDLER_DI_KEY,
                ),
                sessionMetadata = SessionMetadata(
                    integrationType = integrationType,
                    availablePaymentMethods = configurationDataSource.get().paymentMethods
                        .map { it.type }
                        .distinct(),
                ),
            )
        }
    }
}
