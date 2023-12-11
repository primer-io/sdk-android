package io.primer.android.analytics.data.repository

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@ExperimentalCoroutinesApi
internal data class AnalyticsInitDataRepository(
    private val analyticsDataSender: AnalyticsDataSender,
    private val localAnalyticsDataSource: LocalAnalyticsDataSource,
    private val fileAnalyticsDataSource: FileAnalyticsDataSource
) : LifecycleEventObserver {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mutex: Mutex = Mutex()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        send()
    }

    private fun send() = scope.launch {
        mutex.withLock {
            fileAnalyticsDataSource.get().filterNot { it.isEmpty() }
                .flatMapLatest {
                    localAnalyticsDataSource.addEvents(it)
                    analyticsDataSender.sendEvents(it)
                }.catch { expected: Throwable -> expected.printStackTrace() }
                .onEach { sentEvents ->
                    localAnalyticsDataSource.remove(sentEvents)
                }.onCompletion {
                    fileAnalyticsDataSource.update(localAnalyticsDataSource.get())
                }.map { }
                .collect()
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_STOP -> scope.launch {
                mutex.withLock {
                    analyticsDataSender.sendEvents(localAnalyticsDataSource.get())
                }.catch { expected: Throwable -> expected.printStackTrace() }
                    .onEach { sentEvents ->
                        localAnalyticsDataSource.remove(sentEvents)
                    }.onCompletion {
                        fileAnalyticsDataSource.update(localAnalyticsDataSource.get())
                    }.map { }
                    .collect()
            }

            else -> Unit
        }
    }
}
