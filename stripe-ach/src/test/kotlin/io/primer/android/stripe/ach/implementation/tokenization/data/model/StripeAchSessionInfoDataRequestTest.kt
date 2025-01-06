package io.primer.android.stripe.ach.implementation.tokenization.data.model

import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class StripeAchSessionInfoDataRequestTest {
    @Test
    fun `StripeAchSessionInfoDataRequest serializer should return correct platform and locale`() {
        val request =
            StripeAchSessionInfoDataRequest.serializer.serialize(
                StripeAchSessionInfoDataRequest(
                    locale = "fake_locale",
                ),
            )
        assertEquals("ANDROID", request.getString("platform"))
        assertEquals("fake_locale", request.getString("locale"))
    }
}
