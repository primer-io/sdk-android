package io.primer.android.analytics.infrastructure.datasource.connectivity

import io.primer.android.analytics.data.models.CrashProperties
import io.primer.android.core.data.datasource.BaseFlowDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull

internal class UncaughtHandlerDataSource(
    private val defaultExceptionHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler(),
) : BaseFlowDataSource<CrashProperties, Unit>, Thread.UncaughtExceptionHandler {
    private val sharedFlow = MutableStateFlow<CrashProperties?>(null)

    override fun uncaughtException(
        t: Thread,
        e: Throwable,
    ) {
        sharedFlow.tryEmit(
            CrashProperties(
                listOf(e.message.orEmpty()).plus(e.stackTrace.map { it.toString() }),
            ),
        )
        defaultExceptionHandler?.uncaughtException(t, e)
    }

    override fun execute(input: Unit) = sharedFlow.asStateFlow().filterNotNull()
}
