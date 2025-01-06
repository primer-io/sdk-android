package io.primer.android.analytics.data.datasource

import io.primer.android.analytics.data.models.TimerProperties
import io.primer.android.core.data.datasource.BaseFlowDataSource
import io.primer.android.core.utils.EventFlowProvider
import kotlinx.coroutines.flow.filterNotNull

internal class TimerDataSource(private val timerEventProvider: EventFlowProvider<TimerProperties>) :
    BaseFlowDataSource<TimerProperties, Unit> {
    override fun execute(input: Unit) = timerEventProvider.getEventProvider().filterNotNull()
}
