package io.primer.android.analytics.domain.models

import io.primer.android.analytics.data.models.TimerId
import io.primer.android.analytics.data.models.TimerType

internal data class TimerAnalyticsParams(val id: TimerId, val timerType: TimerType) :
    BaseAnalyticsParams()
