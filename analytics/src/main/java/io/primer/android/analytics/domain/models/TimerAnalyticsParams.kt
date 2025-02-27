package io.primer.android.analytics.domain.models

import io.primer.android.analytics.data.models.AnalyticsContext
import io.primer.android.analytics.data.models.TimerId
import io.primer.android.analytics.data.models.TimerType

data class TimerAnalyticsParams(
    val id: TimerId,
    val timerType: TimerType,
    val duration: Long? = null,
    val context: AnalyticsContext? = null,
) : BaseAnalyticsParams()
