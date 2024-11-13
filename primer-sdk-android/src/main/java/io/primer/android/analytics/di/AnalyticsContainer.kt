package io.primer.android.analytics.di

import io.primer.android.HttpClientFactory
import io.primer.android.analytics.data.datasource.LocalAnalyticsDataSource
import io.primer.android.analytics.data.datasource.RemoteAnalyticsDataSource
import io.primer.android.analytics.data.helper.AnalyticsDataSender
import io.primer.android.analytics.data.repository.AnalyticsDataRepository
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.analytics.infrastructure.datasource.BatteryLevelDataSource
import io.primer.android.analytics.infrastructure.datasource.BatteryStatusDataSource
import io.primer.android.analytics.infrastructure.datasource.DeviceIdDataSource
import io.primer.android.analytics.infrastructure.datasource.FileAnalyticsDataSource
import io.primer.android.analytics.infrastructure.datasource.NetworkTypeDataSource
import io.primer.android.analytics.infrastructure.datasource.ScreenSizeDataSource
import io.primer.android.analytics.infrastructure.datasource.connectivity.UncaughtHandlerDataSource
import io.primer.android.analytics.infrastructure.files.AnalyticsFileProvider
import io.primer.android.core.logging.BlacklistedHttpHeaderProviderRegistry
import io.primer.android.core.logging.WhitelistedHttpBodyKeyProviderRegistry
import io.primer.android.di.DependencyContainer
import io.primer.android.di.SdkContainer
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal class AnalyticsContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton {
            PrimerHttpClient(
                HttpClientFactory(
                    logReporter = sdk.resolve(),
                    blacklistedHttpHeaderProviderRegistry =
                    sdk.resolve<BlacklistedHttpHeaderProviderRegistry>(),
                    whitelistedHttpBodyKeyProviderRegistry =
                    sdk.resolve<WhitelistedHttpBodyKeyProviderRegistry>(),
                    localConfigurationDataSource = sdk.resolve()
                ).build(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        val localAnalyticsDataSource = LocalAnalyticsDataSource.instance
        val analyticsFileProvider = AnalyticsFileProvider(sdk.resolve())
        val fileAnalyticsDataSource = FileAnalyticsDataSource(analyticsFileProvider)
        val analyticsDataSender = AnalyticsDataSender(
            RemoteAnalyticsDataSource(resolve())
        )

        registerSingleton<AnalyticsRepository> {
            AnalyticsDataRepository(
                sdk.resolve(),
                sdk.resolve(),
                analyticsDataSender,
                localAnalyticsDataSource,
                fileAnalyticsDataSource,
                ScreenSizeDataSource(sdk.resolve()),
                BatteryLevelDataSource(sdk.resolve()),
                BatteryStatusDataSource(sdk.resolve()),
                DeviceIdDataSource(sdk.resolve()),
                sdk.resolve(),
                NetworkTypeDataSource(sdk.resolve()),
                UncaughtHandlerDataSource().also {
                    Thread.setDefaultUncaughtExceptionHandler(it)
                },
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton { AnalyticsInteractor(resolve(), sdk.resolve()) }
    }
}
