package io.primer.android.components.implementation.presentation

import io.primer.android.analytics.data.models.TimerId
import io.primer.android.analytics.data.models.TimerType
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.TimerAnalyticsParams
import io.primer.android.components.implementation.domain.PaymentsTypesInteractor
import io.primer.android.configuration.data.datasource.GlobalCacheConfigurationCacheDataSource
import io.primer.android.core.domain.None
import io.primer.android.core.utils.CoroutineScopeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.TimeSource

internal interface HeadlessUniversalCheckoutDelegate {
    fun start()

    fun addAnalyticsEvent(params: BaseAnalyticsParams)

    fun clear(exception: CancellationException?)
}

internal class DefaultHeadlessUniversalCheckoutDelegate(
    private val paymentsTypesInteractor: PaymentsTypesInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val globalCacheConfigurationCacheDataSource: GlobalCacheConfigurationCacheDataSource,
    private val scopeProvider: CoroutineScopeProvider,
) : HeadlessUniversalCheckoutDelegate {
    private val scope: CoroutineScope = scopeProvider.scope

    override fun start() {
        scope.launch {
            val timeSource = TimeSource.Monotonic
            val start = timeSource.markNow()
            addAnalyticsEvent(
                TimerAnalyticsParams(
                    id = TimerId.HEADLESS_LOADING,
                    timerType = TimerType.START,
                ),
            )
            paymentsTypesInteractor(None)
            addAnalyticsEvent(
                TimerAnalyticsParams(
                    id = TimerId.HEADLESS_LOADING,
                    timerType = TimerType.END,
                    duration = (timeSource.markNow() - start).inWholeMilliseconds,
                ),
            )
        }
    }

    override fun addAnalyticsEvent(params: BaseAnalyticsParams) {
        scope.launch {
            analyticsInteractor(params)
        }
    }

    override fun clear(exception: CancellationException?) {
        globalCacheConfigurationCacheDataSource.clear()
        scope.coroutineContext.cancelChildren()
    }
}
