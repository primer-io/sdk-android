package io.primer.android.analytics.data.helper

import io.primer.android.analytics.data.models.TimerProperties
import kotlinx.coroutines.flow.MutableStateFlow

internal class TimerEventProvider {
    private val sharedFlow = MutableStateFlow<TimerProperties?>(null)

    fun getTimerEventProvider() = sharedFlow
}
