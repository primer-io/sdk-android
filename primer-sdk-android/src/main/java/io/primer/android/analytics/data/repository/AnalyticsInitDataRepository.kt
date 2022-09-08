package io.primer.android.analytics.data.repository

import io.primer.android.analytics.data.datasource.LocalAnalyticsDataSource
import io.primer.android.analytics.data.helper.AnalyticsDataSender
import io.primer.android.analytics.infrastructure.datasource.FileAnalyticsDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
internal data class AnalyticsInitDataRepository(
    private val analyticsDataSender: AnalyticsDataSender,
    private val localAnalyticsDataSource: LocalAnalyticsDataSource,
    private val fileAnalyticsDataSource: FileAnalyticsDataSource
) {
    init {
        send()
    }

    private fun send() = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
        fileAnalyticsDataSource.get().filterNot { it.isEmpty() }
            .flatMapLatest {
                localAnalyticsDataSource.addEvents(it)
                analyticsDataSender.sendEvents(it)
            }.catch { it.printStackTrace() }
            .collect { }
    }
}
