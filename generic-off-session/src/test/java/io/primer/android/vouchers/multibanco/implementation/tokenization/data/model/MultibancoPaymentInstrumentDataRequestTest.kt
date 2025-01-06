package io.primer.android.vouchers.multibanco.implementation.tokenization.data.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.vouchers.multibanco.implementation.tokenization.data.model.MultibancoPaymentInstrumentDataRequest.Companion.serializer
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MultibancoPaymentInstrumentDataRequestTest {
    @Test
    fun `test MultibancoPaymentInstrumentDataRequest serialization`() {
        val sessionInfo =
            MultibancoSessionInfoDataRequest(
                locale = "en-US",
            )

        val request =
            MultibancoPaymentInstrumentDataRequest(
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
    fun `test MultibancoSessionInfoDataRequest serialization`() {
        val sessionInfo =
            MultibancoSessionInfoDataRequest(
                locale = "en-US",
            )

        val expectedJson =
            JSONObject().apply {
                put(
                    "locale",
                    "en-US",
                )
                put(
                    "platform",
                    "ANDROID",
                )
            }

        val serializedJson =
            JSONSerializationUtils.getJsonObjectSerializer<MultibancoSessionInfoDataRequest>().serialize(sessionInfo)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }
}
