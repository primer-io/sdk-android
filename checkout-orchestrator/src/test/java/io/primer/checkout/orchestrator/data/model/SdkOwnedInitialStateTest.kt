package io.primer.checkout.orchestrator.data.model

import io.primer.statetransport.domain.model.CurrentAttempt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class SdkOwnedInitialStateTest {

    @Test
    fun `serializer should write the sdk urls and envelope values`() {
        val json = SdkOwnedInitialState.serializer.serialize(
            SdkOwnedInitialState(
                sdk = SdkOwnedInitialState.SdkUrls(pciUrl = "https://pci", coreUrl = "https://core"),
                currentAttempt = CurrentAttempt(
                    id = "attempt-1",
                    paymentId = "pay-1",
                ),
            ),
        )

        assertEquals("https://pci", json.getJSONObject("sdk").getString("pciUrl"))
        assertEquals("https://core", json.getJSONObject("sdk").getString("coreUrl"))
        assertEquals("attempt-1", json.getJSONObject("currentAttempt").getString("id"))
        assertEquals("pay-1", json.getJSONObject("currentAttempt").getString("paymentId"))
    }

    @Test
    fun `serializer should omit null envelope values`() {
        val json = SdkOwnedInitialState.serializer.serialize(
            SdkOwnedInitialState(
                sdk = SdkOwnedInitialState.SdkUrls(pciUrl = "https://pci", coreUrl = "https://core"),
                currentAttempt = null,
            ),
        )

        assertFalse(json.has("currentAttempt"))
    }

    @Test
    fun `SDK_OWNED_KEYS should cover every key the serializer can write`() {
        val json = SdkOwnedInitialState.serializer.serialize(
            SdkOwnedInitialState(
                sdk = SdkOwnedInitialState.SdkUrls(pciUrl = "https://pci", coreUrl = "https://core"),
                currentAttempt = CurrentAttempt(id = "attempt-1"),
            ),
        )

        assertEquals(SdkOwnedInitialState.SDK_OWNED_KEYS.sorted(), json.keys().asSequence().toList().sorted())
    }
}
