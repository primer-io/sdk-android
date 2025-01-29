package io.primer.android.analytics.di

import io.primer.android.analytics.data.datasource.CheckoutSessionIdDataSource
import io.primer.android.analytics.data.datasource.CheckoutSessionIdProvider
import io.primer.android.analytics.data.datasource.LocalAnalyticsDataSource
import io.primer.android.analytics.data.datasource.MessagePropertiesDataSource
import io.primer.android.analytics.data.datasource.RemoteAnalyticsDataSource
import io.primer.android.analytics.data.datasource.TimerDataSource
import io.primer.android.analytics.data.helper.AnalyticsDataSender
import io.primer.android.analytics.data.helper.MessagePropertiesEventProvider
import io.primer.android.analytics.data.helper.TimerEventProvider
import io.primer.android.analytics.data.interceptors.HttpAnalyticsInterceptor
import io.primer.android.analytics.data.models.AnalyticsData
import io.primer.android.analytics.data.models.AnalyticsProviderData
import io.primer.android.analytics.data.models.TimerProperties
import io.primer.android.analytics.data.network.HttpClientFactory
import io.primer.android.analytics.data.repository.AnalyticsDataRepository
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.analytics.infrastructure.datasource.BatteryLevelDataSource
import io.primer.android.analytics.infrastructure.datasource.BatteryStatusDataSource
import io.primer.android.analytics.infrastructure.datasource.DeviceIdDataSource
import io.primer.android.analytics.infrastructure.datasource.FileAnalyticsDataSource
import io.primer.android.analytics.infrastructure.datasource.MetaDataSource
import io.primer.android.analytics.infrastructure.datasource.NetworkTypeDataSource
import io.primer.android.analytics.infrastructure.datasource.ScreenSizeDataSource
import io.primer.android.analytics.infrastructure.datasource.connectivity.UncaughtHandlerDataSource
import io.primer.android.analytics.infrastructure.files.AnalyticsFileProvider
import io.primer.android.analytics.utils.Constants
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.helpers.MessageLog
import io.primer.android.core.data.network.helpers.MessagePropertiesHelper
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.core.utils.EventFlowProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Interceptor

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    @Suppress("LongMethod")
    override fun registerInitialDependencies() {
        registerSingleton<CheckoutSessionIdProvider>(CHECKOUT_SESSION_ID_PROVIDER_DI_KEY) {
            CheckoutSessionIdDataSource()
        }

        val interceptor = HttpAnalyticsInterceptor()

        registerSingleton(HTTP_INTERCEPTOR_DI_KEY) { interceptor }
        registerSingleton<Interceptor>(HTTP_INTERCEPTOR_DI_KEY) { interceptor }

        registerSingleton<EventFlowProvider<MessageLog>>(MESSAGE_LOG_PROVIDER_DI_KEY) {
            EventFlowProvider { MutableStateFlow(null) }
        }

        registerSingleton<EventFlowProvider<MessagePropertiesHelper>>(MESSAGE_PROPERTIES_PROVIDER_DI_KEY) {
            MessagePropertiesEventProvider()
        }

        registerSingleton { MessagePropertiesDataSource(resolve(MESSAGE_PROPERTIES_PROVIDER_DI_KEY)) }

        registerSingleton<BaseDataProvider<String?>>(PCI_URL_PROVIDER_INTERNAL_DI_KEY) {
            BaseDataProvider {
                runCatching {
                    sdk().resolve<BaseDataProvider<String>>(dependencyName = PCI_URL_PROVIDER_DI_KEY).provide()
                }.getOrElse { null }
            }
        }

        registerSingleton(HTTP_CLIENT_DI_KEY) {
            PrimerHttpClient(
                okHttpClient =
                HttpClientFactory(
                    logReporter = sdk().resolve(),
                    blacklistedHttpHeaderProviderRegistry =
                    sdk().resolve(),
                    whitelistedHttpBodyKeyProviderRegistry =
                    sdk().resolve(),
                    pciUrlProvider = resolve(PCI_URL_PROVIDER_INTERNAL_DI_KEY),
                ).build(),
                logProvider = resolve(MESSAGE_LOG_PROVIDER_DI_KEY),
                messagePropertiesEventProvider = resolve(MESSAGE_PROPERTIES_PROVIDER_DI_KEY),
            )
        }

        registerSingleton<EventFlowProvider<TimerProperties>>(TIMER_PROPERTIES_PROVIDER_DI_KEY) {
            TimerEventProvider()
        }

        registerSingleton { TimerDataSource(resolve(TIMER_PROPERTIES_PROVIDER_DI_KEY)) }

        registerSingleton { MetaDataSource(sdk().resolve()) }

        registerFactory(name = Constants.APPLICATION_ID_PROVIDER_DI_KEY) {
            BaseDataProvider { sdk().resolve<MetaDataSource>().getApplicationId() }
        }

        val localAnalyticsDataSource = LocalAnalyticsDataSource.instance
        val analyticsFileProvider = AnalyticsFileProvider(sdk().resolve())
        val fileAnalyticsDataSource = FileAnalyticsDataSource(analyticsFileProvider)
        val analyticsDataSender =
            AnalyticsDataSender(
                RemoteAnalyticsDataSource(resolve(HTTP_CLIENT_DI_KEY)),
            )

        registerSingleton<BaseDataProvider<AnalyticsProviderData>>(
            name = ANALYTICS_PROVIDER_DATA_DI_KEY,
        ) {
            BaseDataProvider {
                AnalyticsProviderData(
                    sdk().resolve<MetaDataSource>().getApplicationId(),
                    runCatching {
                        sdk().resolve<BaseDataProvider<AnalyticsData>>(ANALYTICS_DATA_DI_KEY)
                            .provide()
                    }.getOrNull(),
                )
            }
        }

        registerSingleton<AnalyticsRepository> {
            AnalyticsDataRepository(
                analyticsDataSender = analyticsDataSender,
                localAnalyticsDataSource = localAnalyticsDataSource,
                fileAnalyticsDataSource = fileAnalyticsDataSource,
                screenSizeDataSource = ScreenSizeDataSource(sdk().resolve()),
                batteryLevelDataSource = BatteryLevelDataSource(sdk().resolve()),
                batteryStatusDataSource = BatteryStatusDataSource(sdk().resolve()),
                deviceIdDataSource = DeviceIdDataSource(sdk().resolve()),
                networkTypeDataSource = NetworkTypeDataSource(sdk().resolve()),
                uncaughtHandlerDataSource =
                UncaughtHandlerDataSource().also {
                    Thread.setDefaultUncaughtExceptionHandler(it)
                },
                networkCallDataSource = sdk().resolve(HTTP_INTERCEPTOR_DI_KEY),
                timerDataSource = sdk().resolve(),
                checkoutSessionIdDataSource = resolve(CHECKOUT_SESSION_ID_PROVIDER_DI_KEY),
                provider =
                sdk().resolve(
                    ANALYTICS_PROVIDER_DATA_DI_KEY,
                ),
                messagePropertiesDataSource = resolve(),
            )
        }

        registerSingleton { AnalyticsInteractor(resolve(), sdk().resolve()) }
    }

    companion object {
        private const val HTTP_CLIENT_DI_KEY = "ANALYTICS_HTTP_CLIENT"
        private const val ANALYTICS_PROVIDER_DATA_DI_KEY = "ANALYTICS_PROVIDER_DATA"
        const val HTTP_INTERCEPTOR_DI_KEY = "ANALYTICS_HTTP_INTERCEPTOR"
        const val ANALYTICS_DATA_DI_KEY = "ANALYTICS_DATA"
        const val CHECKOUT_SESSION_ID_PROVIDER_DI_KEY = "CHECKOUT_SESSION_ID_PROVIDER"
        const val MESSAGE_LOG_PROVIDER_DI_KEY = "MESSAGE_LOG_PROVIDER"
        const val TIMER_PROPERTIES_PROVIDER_DI_KEY = "TIMER_PROPERTIES_PROVIDER"
        const val MESSAGE_PROPERTIES_PROVIDER_DI_KEY = "MESSAGE_PROPERTIES_PROVIDER"
        const val PCI_URL_PROVIDER_INTERNAL_DI_KEY = "PCI_URL_PROVIDER_INTERNAL"
        const val PCI_URL_PROVIDER_DI_KEY = "PCI_URL_PROVIDER"
    }
}
