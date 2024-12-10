package io.primer.android.sandboxProcessor.klarna

import io.primer.android.core.utils.Success
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class SandboxProcessorKlarnaFactorySandboxProcessor {
    @Test
    fun `build should return success for PRIMER_TEST_KLARNA`() {
        val result = SandboxProcessorKlarnaFactory("PRIMER_TEST_KLARNA").build()
        assertIs<Success<SandboxProcessorKlarna, Exception>>(result)
    }
}
