package io.primer.android.analytics.domain.repository

import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    suspend fun startObservingEvents()

    fun addEvent(params: BaseAnalyticsParams)

    fun send(): Flow<Unit>
}
