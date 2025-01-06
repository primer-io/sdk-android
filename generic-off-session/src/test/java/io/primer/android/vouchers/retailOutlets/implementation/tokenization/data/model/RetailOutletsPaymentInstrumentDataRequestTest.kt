package io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.model.RetailOutletsPaymentInstrumentDataRequest.Companion.serializer
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RetailOutletsPaymentInstrumentDataRequestTest {
    @Test
    fun `test RetailOutletsPaymentInstrumentDataRequest serialization`() {
        val sessionInfo =
            RetailOutletsSessionInfoDataRequest(
                locale = "en-US",
                retailerOutlet = "testRetailOutlet",
            )

        val request =
            RetailOutletsPaymentInstrumentDataRequest(
                paymentMethodType = "MULTIBANCO",
                paymentMethodConfigId = "config-id",
                sessionInfo = sessionInfo,
                type = PaymentInstrumentType.OFF_SESSION_PAYMENT,
            )

        val expectedJson =
            JSONObject().apply {
                put(
                    "type",
                    "OFF_SESSION_PAYMENT",
                )
                put(
                    "paymentMethodType",
                    "MULTIBANCO",
                )
                put(
                    "paymentMethodConfigId",
                    "config-id",
                )
                put(
                    "sessionInfo",
                    JSONObject().apply {
                        put(
                            "retailOutlet",
                            "testRetailOutlet",
                        )
                        put(
                            "locale",
                            "en-US",
                        )
                        put(
                            "platform",
                            "ANDROID",
                        )
                    },
                )
            }

        val serializedJson = serializer.serialize(request)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }

    @Test
    fun `test RetailOutletsSessionInfoDataRequest serialization`() {
        val sessionInfo =
            RetailOutletsSessionInfoDataRequest(
                locale = "en-US",
                retailerOutlet = "testRetailOutlet",
            )

        val expectedJson =
            JSONObject().apply {
                put(
                    "locale",
                    "en-US",
                )
                put(
                    "retailOutlet",
                    "testRetailOutlet",
                )
                put(
                    "platform",
                    "ANDROID",
                )
            }

        val serializedJson =
            JSONSerializationUtils.getJsonObjectSerializer<RetailOutletsSessionInfoDataRequest>()
                .serialize(sessionInfo)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }
}
