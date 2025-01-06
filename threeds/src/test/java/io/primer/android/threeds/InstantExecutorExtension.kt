package io.primer.android.threeds

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

@ExperimentalCoroutinesApi
class InstantExecutorExtension(
    private val scheduler: TestCoroutineScheduler = TestCoroutineScheduler(),
    val dispatcher: TestDispatcher = StandardTestDispatcher(scheduler),
) : BeforeEachCallback, AfterEachCallback, AfterTestExecutionCallback {
    override fun afterTestExecution(context: ExtensionContext?) {
        scheduler.advanceUntilIdle()
    }

    override fun beforeEach(extensionContext: ExtensionContext) {
        Dispatchers.setMain(dispatcher)
        ArchTaskExecutor.getInstance().setDelegate(
            object : TaskExecutor() {
                override fun executeOnDiskIO(runnable: Runnable) = runnable.run()

                override fun postToMainThread(runnable: Runnable) = runnable.run()

                override fun isMainThread() = true
            },
        )
    }

    override fun afterEach(context: ExtensionContext?) {
        Dispatchers.resetMain()
        ArchTaskExecutor.getInstance().setDelegate(null)
    }
}
