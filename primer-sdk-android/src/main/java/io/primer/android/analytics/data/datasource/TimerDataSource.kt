package io.primer.android.analytics.data.datasource

import io.primer.android.analytics.data.helper.TimerEventProvider
import io.primer.android.analytics.data.models.TimerProperties
import io.primer.android.data.base.datasource.BaseFlowDataSource
import kotlinx.coroutines.flow.filterNotNull

internal class TimerDataSource(private val timerEventProvider: TimerEventProvider) :
    BaseFlowDataSource<TimerProperties, Unit> {

    override fun execute(input: Unit) = timerEventProvider.getTimerEventProvider().filterNotNull()
}
