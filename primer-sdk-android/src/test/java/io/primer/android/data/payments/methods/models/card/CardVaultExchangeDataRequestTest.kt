package io.primer.android.data.payments.methods.models.card

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class CardVaultExchangeDataRequestTest {

    @Test
    fun `CardVaultExchangeDataRequest should be serialized correctly`() {
        val expectedJson = """{"cvv":"123"}"""
        assertEquals(
            expectedJson,
            CardVaultExchangeDataRequest.serializer.serialize(
                CardVaultExchangeDataRequest("123")
            ).toString()
        )
    }
}
