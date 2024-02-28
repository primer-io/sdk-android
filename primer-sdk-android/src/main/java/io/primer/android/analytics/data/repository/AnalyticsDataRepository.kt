package io.primer.android.analytics.data.repository

import io.primer.android.analytics.data.datasource.CheckoutSessionIdDataSource
import io.primer.android.analytics.data.datasource.LocalAnalyticsDataSource
import io.primer.android.analytics.data.datasource.SdkSessionDataSource
import io.primer.android.analytics.data.datasource.TimerDataSource
import io.primer.android.analytics.data.helper.AnalyticsDataSender
import io.primer.android.analytics.data.interceptors.NetworkCallDataSource
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
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.infrastructure.metadata.datasource.MetaDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

@Suppress("LongParameterList")
@ExperimentalCoroutinesApi
internal class AnalyticsDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val localClientTokenDataSource: LocalClientTokenDataSource,
    private val analyticsDataSender: AnalyticsDataSender,
    private val localAnalyticsDataSource: LocalAnalyticsDataSource,
    private val fileAnalyticsDataSource: FileAnalyticsDataSource,
    private val screenSizeDataSource: ScreenSizeDataSource,
    private val batteryLevelDataSource: BatteryLevelDataSource,
    private val batteryStatusDataSource: BatteryStatusDataSource,
    private val deviceIdDataSource: DeviceIdDataSource,
    private val metaDataSource: MetaDataSource,
    private val networkTypeDataSource: NetworkTypeDataSource,
    private val uncaughtHandlerDataSource: UncaughtHandlerDataSource,
    private val networkCallDataSource: NetworkCallDataSource,
    private val timerDataSource: TimerDataSource,
    private val checkoutSessionIdDataSource: CheckoutSessionIdDataSource,
    private val settings: PrimerSettings
) : AnalyticsRepository {

    private val sdkSessionId by lazy { SdkSessionDataSource.getSessionId() }

    override fun initialize() = flowOf(
        networkCallDataSource.execute(Unit),
        uncaughtHandlerDataSource.execute(Unit),
        networkTypeDataSource.execute(Unit),
        timerDataSource.execute(Unit)
    ).flattenMerge()
        .flatMapLatest {
            val configuration =
                localConfigurationDataSource.getConfigurationNullable()

            flowOf(
                localAnalyticsDataSource.addEvent(
                    it.toAnalyticsEvent(
                        batteryLevelDataSource.get(),
                        batteryStatusDataSource.get(),
                        screenSizeDataSource.get(),
                        deviceIdDataSource.get(),
                        metaDataSource.getApplicationId(),
                        sdkSessionId,
                        settings.sdkIntegrationType,
                        settings.paymentHandling,
                        checkoutSessionIdDataSource.checkoutSessionId,
                        configuration?.clientSession?.clientSessionId,
                        configuration?.clientSession?.order?.orderId,
                        configuration?.primerAccountId,
                        localClientTokenDataSource.get().analyticsUrlV2
                    )
                )
            )
        }.onEach { fileAnalyticsDataSource.update(localAnalyticsDataSource.get()) }
        .mapLatest { }

    override fun addEvent(params: BaseAnalyticsParams) {
        val configuration = localConfigurationDataSource.getConfigurationNullable()
        localAnalyticsDataSource.addEvent(
            params.toAnalyticsEvent(
                batteryLevelDataSource.get(),
                batteryStatusDataSource.get(),
                screenSizeDataSource.get(),
                deviceIdDataSource.get(),
                metaDataSource.getApplicationId(),
                sdkSessionId,
                settings.sdkIntegrationType,
                settings.paymentHandling,
                checkoutSessionIdDataSource.checkoutSessionId,
                configuration?.clientSession?.clientSessionId,
                configuration?.clientSession?.order?.orderId,
                configuration?.primerAccountId,
                localClientTokenDataSource.get().analyticsUrlV2
            )
        )
        fileAnalyticsDataSource.update(localAnalyticsDataSource.get())
    }

    override fun send() = analyticsDataSender.sendEvents(localAnalyticsDataSource.get())
        .onEach { sentEvents ->
            localAnalyticsDataSource.remove(sentEvents)
        }.onCompletion {
            fileAnalyticsDataSource.update(localAnalyticsDataSource.get())
        }.map { }
}
