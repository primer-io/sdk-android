package io.primer.android.components.presentation

import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.components.domain.payments.PaymentsTypesInteractor
import io.primer.android.domain.base.None
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

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
            paymentsTypesInteractor(None()).collect {}
        }
    }

    override fun addAnalyticsEvent(params: BaseAnalyticsParams) {
        scope.launch {
            analyticsInteractor(params).collect {}
        }
    }

    override fun clear(exception: CancellationException?) =
        scope.coroutineContext.job.cancelChildren(exception)
}
