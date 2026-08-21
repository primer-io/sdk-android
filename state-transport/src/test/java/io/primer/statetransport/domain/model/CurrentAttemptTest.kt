package io.primer.statetransport.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class CurrentAttemptTest {

    @Test
    fun `serializer should write the id and payment identifiers`() {
        val json = CurrentAttempt.serializer.serialize(
            CurrentAttempt(
                id = "attempt-1",
                paymentInstrumentTokenId = "token-1",
                paymentId = "pay-1",
            ),
        )

        assertEquals("attempt-1", json.getString("id"))
        assertEquals("token-1", json.getString("paymentInstrumentTokenId"))
        assertEquals("pay-1", json.getString("paymentId"))
    }

    @Test
    fun `serializer should omit null fields`() {
        val json = CurrentAttempt.serializer.serialize(CurrentAttempt(id = "attempt-1"))

        assertEquals("attempt-1", json.getString("id"))
        assertFalse(json.has("paymentInstrumentTokenId"))
        assertFalse(json.has("paymentId"))
    }
}
