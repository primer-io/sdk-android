package io.primer.android.sandboxProcessor.paypal

import io.primer.android.core.utils.Success
import io.primer.android.sandboxProcessor.klarna.SandboxProcessorKlarnaFactory
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class SandboxPayPalFactorySandboxProcessorProcessor {
    @Test
    fun `build should return success for PRIMER_TEST_PAYPAL`() {
        val result = SandboxProcessorKlarnaFactory("PRIMER_TEST_PAYPAL").build()
        assertIs<Success<SandboxProcessorPayPal, Exception>>(result)
    }
}
