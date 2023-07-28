package io.primer.android.data.payments.methods.models.empty

import org.json.JSONObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class EmptyVaultExchangeDataRequestTest {

    @Test
    fun `EmptyExchangeDataRequest should be serialized correctly`() {
        assertEquals(
            JSONObject().toString(),
            EmptyExchangeDataRequest.serializer.serialize(
                EmptyExchangeDataRequest()
            ).toString()
        )
    }
}
