package io.primer.android.analytics.data.repository

import io.primer.android.analytics.data.datasource.CheckoutSessionIdProvider
import io.primer.android.analytics.data.datasource.LocalAnalyticsDataSource
import io.primer.android.analytics.data.datasource.MessagePropertiesDataSource
import io.primer.android.analytics.data.datasource.SdkSessionDataSource
import io.primer.android.analytics.data.datasource.TimerDataSource
import io.primer.android.analytics.data.helper.AnalyticsDataSender
import io.primer.android.analytics.data.interceptors.NetworkCallDataSource
import io.primer.android.analytics.data.models.AnalyticsProviderData
import io.primer.android.analytics.data.models.toAnalyticsEvent
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.analytics.infrastructure.datasource.BatteryLevelDataSource
import io.primer.android.analytics.infrastructure.datasource.BatteryStatusDataSource
import io.primer.android.analytics.infrastructure.datasource.DeviceIdDataSource
import io.primer.android.analytics.infrastructure.datasource.FileAnalyticsDataSource
import io.primer.android.analytics.infrastructure.datasource.NetworkTypeDataSource
import io.primer.android.analytics.infrastructure.datasource.ScreenSizeDataSource
import io.primer.android.analytics.infrastructure.datasource.connectivity.UncaughtHandlerDataSource
import io.primer.android.core.utils.BaseDataProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

@Suppress("LongParameterList")
@ExperimentalCoroutinesApi
internal class AnalyticsDataRepository(
    private val analyticsDataSender: AnalyticsDataSender,
    private val localAnalyticsDataSource: LocalAnalyticsDataSource,
    private val fileAnalyticsDataSource: FileAnalyticsDataSource,
    private val screenSizeDataSource: ScreenSizeDataSource,
    private val batteryLevelDataSource: BatteryLevelDataSource,
    private val batteryStatusDataSource: BatteryStatusDataSource,
    private val deviceIdDataSource: DeviceIdDataSource,
    private val networkTypeDataSource: NetworkTypeDataSource,
    private val uncaughtHandlerDataSource: UncaughtHandlerDataSource,
    private val networkCallDataSource: NetworkCallDataSource,
    private val timerDataSource: TimerDataSource,
    private val checkoutSessionIdDataSource: CheckoutSessionIdProvider,
    private val provider: BaseDataProvider<AnalyticsProviderData>,
    private val messagePropertiesDataSource: MessagePropertiesDataSource,
) : AnalyticsRepository {
    private val sdkSessionId by lazy { SdkSessionDataSource.getSessionId() }

    override suspend fun startObservingEvents() =
        merge(
            networkCallDataSource.execute(Unit),
            uncaughtHandlerDataSource.execute(Unit),
            networkTypeDataSource.execute(Unit),
            timerDataSource.execute(Unit),
            messagePropertiesDataSource.execute(Unit),
        ).mapLatest { properties ->
            val providerData = provider.provide()

            localAnalyticsDataSource.addEvent(
                properties.toAnalyticsEvent(
                    batteryLevel = batteryLevelDataSource.get(),
                    batteryStatus = batteryStatusDataSource.get(),
                    screenData = screenSizeDataSource.get(),
                    deviceId = deviceIdDataSource.get(),
                    appIdentifier = providerData.applicationId,
                    sdkSessionId = sdkSessionId,
                    sdkIntegrationType = providerData.data?.sdkIntegrationType,
                    sdkPaymentHandling = providerData.data?.paymentHandling,
                    checkoutSessionId = checkoutSessionIdDataSource.provide(),
                    clientSessionId = providerData.data?.clientSessionId,
                    orderId = providerData.data?.orderId,
                    primerAccountId = providerData.data?.primerAccountId,
                    analyticsUrl = providerData.data?.analyticsUrl,
                ),
            )
        }.onEach {
            fileAnalyticsDataSource.update(localAnalyticsDataSource.get())
        }.distinctUntilChanged().collect()

    override fun addEvent(params: BaseAnalyticsParams) {
        val providerData = provider.provide()
        localAnalyticsDataSource.addEvent(
            params.toAnalyticsEvent(
                batteryLevel = batteryLevelDataSource.get(),
                batteryStatus = batteryStatusDataSource.get(),
                screenData = screenSizeDataSource.get(),
                deviceId = deviceIdDataSource.get(),
                appIdentifier = providerData.applicationId,
                sdkSessionId = sdkSessionId,
                sdkIntegrationType = providerData.data?.sdkIntegrationType,
                sdkPaymentHandling = providerData.data?.paymentHandling,
                checkoutSessionId = checkoutSessionIdDataSource.provide(),
                clientSessionId = providerData.data?.clientSessionId,
                orderId = providerData.data?.orderId,
                primerAccountId = providerData.data?.primerAccountId,
                analyticsUrl = providerData.data?.analyticsUrl,
            ),
        )
        fileAnalyticsDataSource.update(localAnalyticsDataSource.get())
    }

    override fun send() =
        analyticsDataSender.sendEvents(localAnalyticsDataSource.get())
            .onEach { sentEvents ->
                localAnalyticsDataSource.remove(sentEvents)
            }.onCompletion {
                fileAnalyticsDataSource.update(localAnalyticsDataSource.get())
            }.map { }
}
