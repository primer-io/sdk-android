package io.primer.android.analytics.di

import io.primer.android.HttpClientFactory
import io.primer.android.analytics.data.datasource.LocalAnalyticsDataSource
import io.primer.android.analytics.data.datasource.RemoteAnalyticsDataSource
import io.primer.android.analytics.data.datasource.TimerDataSource
import io.primer.android.analytics.data.helper.AnalyticsDataSender
import io.primer.android.analytics.data.helper.TimerEventProvider
import io.primer.android.analytics.data.interceptors.HttpAnalyticsInterceptor
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
import io.primer.android.http.PrimerHttpClient
import io.primer.android.logging.DefaultLogger
import io.primer.android.logging.Logger
import io.primer.android.presentation.base.BaseViewModel
import okhttp3.Interceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val ANALYTICS_LOGGER_NAME = "ANALYTICS_LOGGER"
private const val ANALYTICS_HTTP_CLIENT_NAME = "ANALYTICS_HTTP_CLIENT"

internal val analyticsModule = {
    module {
        factory<Logger>(named(ANALYTICS_LOGGER_NAME)) { DefaultLogger(ANALYTICS_LOGGER_NAME) }
        single(named(ANALYTICS_HTTP_CLIENT_NAME)) {
            PrimerHttpClient(
                HttpClientFactory().build(),
                get()
            )
        }
        single { ScreenSizeDataSource(get()) }
        single { BatteryLevelDataSource(get()) }
        single { BatteryStatusDataSource(get()) }
        single { DeviceIdDataSource(get()) }
        single { NetworkTypeDataSource(get()) }
        single { UncaughtHandlerDataSource() }
        single { AnalyticsDataSender(get(), get(), get()) }
        single { LocalAnalyticsDataSource.instance }
        single { FileAnalyticsDataSource(get(), get()) }
        single { AnalyticsFileProvider(get()) }
        single { RemoteAnalyticsDataSource(get(named(ANALYTICS_HTTP_CLIENT_NAME))) }
        single { TimerEventProvider() }
        single { TimerDataSource(get()) }
        single<AnalyticsRepository> {
            AnalyticsDataRepository(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get<Interceptor>() as HttpAnalyticsInterceptor,
                get(),
                get()
            )
        }
        single { AnalyticsInteractor(get(), get(named(ANALYTICS_LOGGER_NAME))) }

        viewModel { BaseViewModel(get()) }
    }
}
