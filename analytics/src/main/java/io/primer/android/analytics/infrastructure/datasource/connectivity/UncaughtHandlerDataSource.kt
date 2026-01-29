package io.primer.android.analytics.infrastructure.datasource.connectivity

import io.primer.android.analytics.data.models.CrashProperties
import io.primer.android.core.BuildConfig
import io.primer.android.core.data.datasource.BaseFlowDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull

class UncaughtHandlerDataSource(
    private val defaultExceptionHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler(),
) : BaseFlowDataSource<CrashProperties, Unit>, Thread.UncaughtExceptionHandler {
    private val sharedFlow = MutableStateFlow<CrashProperties?>(null)

    override fun uncaughtException(
        t: Thread,
        e: Throwable,
    ) {
        if (e.isSdkCrash(groupId = BuildConfig.SDK_GROUP_ID)) {
            sharedFlow.tryEmit(
                CrashProperties(
                    listOf(e.stackTraceToString()),
                ),
            )
        }
        defaultExceptionHandler?.uncaughtException(t, e)
    }

    override fun execute(input: Unit) = sharedFlow.asStateFlow().filterNotNull()

    private fun Throwable.isSdkCrash(groupId: String): Boolean {
        return stackTrace.any { it.className.startsWith(groupId) }
    }
}
