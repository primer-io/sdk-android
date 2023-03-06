package io.primer.android.analytics.domain.repository

import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import kotlinx.coroutines.flow.Flow

internal interface AnalyticsRepository {

    fun initialize(): Flow<Unit>

    fun addEvent(params: BaseAnalyticsParams): Unit

    fun send(): Flow<Unit>
}
