package io.primer.android.components.implementation.completion

import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.payments.core.helpers.PollingStartHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class DefaultPollingStartHandlerTest {
    private lateinit var pollingStartHandler: DefaultPollingStartHandler

    @BeforeEach
    fun setUp() {
        pollingStartHandler = DefaultPollingStartHandler()
    }

    @Test
    fun `handle should emit payment to startPolling flow`() =
        runTest {
            val pollingStartData = mockk<PollingStartHandler.PollingStartData>()

            val job =
                launch {
                    val data = pollingStartHandler.startPolling.first()
                    assertEquals(pollingStartData, data)
                }

            pollingStartHandler.handle(pollingStartData)
            job.cancel()
        }
}
