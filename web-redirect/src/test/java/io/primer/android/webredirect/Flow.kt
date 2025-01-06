package io.primer.android.webredirect

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.time.Duration

@ExperimentalCoroutinesApi
suspend fun <T> Flow<T>.toListDuring(
    duration: Duration,
    dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
): List<T> =
    coroutineScope {
        val result = mutableListOf<T>()
        val job =
            launch(dispatcher) {
                this@toListDuring.collect(result::add)
            }
        delay(duration)
        job.cancel()
        return@coroutineScope result
    }
