package io.primer.android.components.analytics.di

import io.primer.android.analytics.data.datasource.CheckoutSessionIdProvider
import io.primer.android.analytics.di.AnalyticsContainer
import io.primer.android.components.analytics.data.provider.DeviceInfoProvider
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepositoryImpl
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.BuildConfig
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.settings.internal.PrimerConfig

class ComponentsAnalyticsContainer(
    private val sdk: () -> SdkContainer,
) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<ComponentsEventsRepository> {
            val configurationDataSource = sdk().resolve<BaseCacheDataSource<ConfigurationData, ConfigurationData>>(
                ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY,
            )
            val configurationData = configurationDataSource.get()

            val checkoutSessionIdProvider = sdk().resolve<CheckoutSessionIdProvider>(
                AnalyticsContainer.CHECKOUT_SESSION_ID_PROVIDER_DI_KEY,
            )

            ComponentsEventsRepositoryImpl(
                logReporter = sdk().resolve<LogReporter>(),
                environment = configurationData.environment,
                configurationData = configurationData,
                checkoutSessionId = checkoutSessionIdProvider.provide(),
                sdkVersion = BuildConfig.SDK_VERSION_STRING,
                httpClient = sdk().resolve(),
                clientToken = sdk().resolve<PrimerConfig>().clientTokenBase64 ?: "",
                deviceInfoProvider = DeviceInfoProvider(context = sdk().resolve()),
            )
        }
    }
}
