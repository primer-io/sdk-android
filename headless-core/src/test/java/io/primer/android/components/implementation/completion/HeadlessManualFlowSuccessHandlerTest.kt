package io.primer.android.components.implementation.completion

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class HeadlessManualFlowSuccessHandlerTest {
    @Test
    fun `handle does nothing`() =
        runTest {
            HeadlessManualFlowSuccessHandler().handle()
        }
}
