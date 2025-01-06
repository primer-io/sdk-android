package io.primer.android.vouchers.retailOutlets.implementation.rpc.data.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RetailOutletDataRequestTest {
    @Test
    fun `test RetailOutletDataRequest serialization`() {
        val paymentMethodConfigId = "config_id_123"
        val dataRequest = RetailOutletDataRequest(paymentMethodConfigId)
        val jsonObject = RetailOutletDataRequest.serializer.serialize(dataRequest)

        assertEquals(paymentMethodConfigId, jsonObject.getString("paymentMethodConfigId"))
    }

    @Test
    fun `test RetailOutletDataResponse toRetailOutlet extension function`() {
        val id = "outlet_id_123"
        val name = "Test Outlet"
        val disabled = false
        val iconUrl = "https://example.com/icon.png"
        val dataResponse = RetailOutletDataResponse(id, name, disabled, iconUrl)

        val retailOutlet = dataResponse.toRetailOutlet()

        assertEquals(id, retailOutlet.id)
        assertEquals(name, retailOutlet.name)
        assertEquals(disabled, retailOutlet.disabled)
        assertEquals(iconUrl, retailOutlet.iconUrl)
    }
}
