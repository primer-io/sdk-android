package io.primer.android.otp

import io.primer.android.core.utils.Success
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class OtpFactoryTest {
    @Test
    fun `build should return success for ADYEN_BLIK`() {
        val result = OtpFactory("ADYEN_BLIK").build()
        assertIs<Success<Otp, Exception>>(result)
    }
}
