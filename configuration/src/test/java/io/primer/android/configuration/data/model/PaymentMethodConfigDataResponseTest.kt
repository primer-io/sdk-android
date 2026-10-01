package io.primer.android.configuration.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class PaymentMethodConfigDataResponseTest {

    @Test
    fun `clientRequirements is passed on as received`() {
        val config = decode(""""clientRequirements": {"stepTypes": {"url.open": "^1.0.0"}}""")

        assertEquals(
            JSONObject("""{"stepTypes": {"url.open": "^1.0.0"}}""").toString(),
            config.clientRequirements,
        )
    }

    @Test
    fun `clientRequirements is null when absent`() {
        assertNull(decode().clientRequirements)
    }

    @Test
    fun `isBackendDriven recognises the new form and the legacy one`() {
        assertTrue(decode(implementationType = "BACKEND_DRIVEN").isBackendDriven)
        assertTrue(
            decode(""""capabilities": ["BACKEND_DRIVEN"]""", implementationType = "WEB_REDIRECT").isBackendDriven,
        )
        assertFalse(decode(""""capabilities": ["BACKEND_DRIVEN"]""", implementationType = "NATIVE_SDK").isBackendDriven)
        assertFalse(decode(implementationType = "WEB_REDIRECT").isBackendDriven)
    }

    private fun decode(
        extraFields: String? = null,
        implementationType: String = "BACKEND_DRIVEN",
    ): PaymentMethodConfigDataResponse {
        val extra = extraFields?.let { ", $it" }.orEmpty()
        return PaymentMethodConfigDataResponse.deserializer.deserialize(
            JSONObject("""{"id": "pm_1", "type": "TWINT", "implementationType": "$implementationType"$extra}"""),
        )
    }
}
