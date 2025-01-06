package io.primer.android.ui.utils

import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.primer.android.InstantExecutorExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class DefaultCheckoutExitHandlerTest {
    @RelaxedMockK
    internal lateinit var onExit: () -> Unit

    private lateinit var checkoutExitHandler: DefaultCheckoutExitHandler

    @BeforeEach
    fun setUp() {
        checkoutExitHandler = DefaultCheckoutExitHandler(onExit)
    }

    @Test
    fun `handle should emit payment to checkoutExited flow`() =
        runTest {
            val job =
                launch {
                    val emission = checkoutExitHandler.checkoutExited.first()
                    assertEquals(Unit, emission)
                }

            checkoutExitHandler.handle()
            job.cancel()
        }

    @Test
    fun `handle should call onExit`() =
        runTest {
            val job =
                launch {
                    val emission = checkoutExitHandler.checkoutExited.first()
                    assertEquals(Unit, emission)
                }

            checkoutExitHandler.handle()

            advanceUntilIdle()

            coVerify {
                onExit.invoke()
            }
            job.cancel()
        }
}
