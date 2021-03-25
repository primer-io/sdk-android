package io.primer.android

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class InstantExecutorExtension(
    private val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher(),
    private val scope: TestCoroutineScope = TestCoroutineScope(dispatcher)
) : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(extensionContext: ExtensionContext) {
        Dispatchers.setMain(
            object : CoroutineDispatcher() {
                override fun dispatch(context: CoroutineContext, block: Runnable) {
                    dispatcher.dispatch(context, block)
                }
            }
        )

        ArchTaskExecutor.getInstance().setDelegate(
            object : TaskExecutor() {
                override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
                override fun postToMainThread(runnable: Runnable) = runnable.run()
                override fun isMainThread() = true
            }
        )
    }

    override fun afterEach(context: ExtensionContext?) {
        dispatcher.cleanupTestCoroutines()
        scope.cleanupTestCoroutines()
        Dispatchers.resetMain()
        ArchTaskExecutor.getInstance().setDelegate(null)
    }
}
