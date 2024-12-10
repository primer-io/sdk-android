package io.primer.android.analytics.data.helper

import io.primer.android.analytics.data.models.TimerProperties
import io.primer.android.core.utils.EventFlowProvider
import kotlinx.coroutines.flow.MutableStateFlow

internal class TimerEventProvider : EventFlowProvider<TimerProperties> {
    private val sharedFlow = MutableStateFlow<TimerProperties?>(null)

    override fun getEventProvider(): MutableStateFlow<TimerProperties?> = sharedFlow
}
