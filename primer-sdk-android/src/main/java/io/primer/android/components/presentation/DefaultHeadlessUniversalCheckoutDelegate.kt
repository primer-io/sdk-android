package io.primer.android.components.presentation

import io.primer.android.analytics.data.models.TimerId
import io.primer.android.analytics.data.models.TimerType
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.TimerAnalyticsParams
import io.primer.android.components.domain.payments.PaymentsTypesInteractor
import io.primer.android.data.configuration.datasource.GlobalConfigurationCacheDataSource
import io.primer.android.domain.base.None
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.job
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
    private val analyticsInteractor: AnalyticsInteractor
) : HeadlessUniversalCheckoutDelegate {

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    override fun start() {
        scope.launch {
            analyticsInteractor.initialize().collect {}
        }
        scope.launch {
            val timeSource = TimeSource.Monotonic
            val start = timeSource.markNow()
            paymentsTypesInteractor(None()).onStart {
                addAnalyticsEvent(
                    TimerAnalyticsParams(
                        id = TimerId.HEADLESS_LOADING,
                        timerType = TimerType.START
                    )
                )
            }.onEach {
                addAnalyticsEvent(
                    TimerAnalyticsParams(
                        id = TimerId.HEADLESS_LOADING,
                        timerType = TimerType.END,
                        duration = (timeSource.markNow() - start).inWholeMilliseconds
                    )
                )
            }.collect {}
        }
    }

    override fun addAnalyticsEvent(params: BaseAnalyticsParams) {
        scope.launch {
            analyticsInteractor(params).collect {}
        }
    }

    override fun clear(exception: CancellationException?) {
        GlobalConfigurationCacheDataSource.clear()
        scope.coroutineContext.job.cancelChildren(exception)
    }
}
