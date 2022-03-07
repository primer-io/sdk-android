package io.primer.android.analytics.infrastructure.datasource.connectivity

import android.os.Process
import io.primer.android.analytics.data.models.CrashProperties
import io.primer.android.data.base.datasource.BaseFlowDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlin.system.exitProcess

internal class UncaughtHandlerDataSource :
    BaseFlowDataSource<CrashProperties, Unit>, Thread.UncaughtExceptionHandler {

    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null
    private val sharedFlow = MutableStateFlow<CrashProperties?>(null)

    init {
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        sharedFlow.tryEmit(
            CrashProperties(
                listOf(e.message.orEmpty()).plus(e.stackTrace.map { it.toString() })
            )
        )
        defaultExceptionHandler?.uncaughtException(t, e) ?: run {
            killProcessAndExit()
        }
    }

    override fun execute(input: Unit) = sharedFlow.asStateFlow().filterNotNull()

    private fun killProcessAndExit() {
        try {
            Thread.sleep(SLEEP_TIMEOUT_MS)
        } catch (ignored: InterruptedException) {
        }
        Process.killProcess(Process.myPid())
        exitProcess(PROCESS_STATUS)
    }

    private companion object {
        const val SLEEP_TIMEOUT_MS = 400L
        const val PROCESS_STATUS = 10
    }
}
