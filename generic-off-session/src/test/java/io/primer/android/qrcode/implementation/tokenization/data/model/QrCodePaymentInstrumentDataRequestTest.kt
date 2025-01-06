package io.primer.android.qrcode.implementation.tokenization.data.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.qrcode.implementation.tokenization.data.model.QrCodePaymentInstrumentDataRequest.Companion.serializer
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class QrCodePaymentInstrumentDataRequestTest {
    @Test
    fun `test QrCodePaymentInstrumentDataRequest serialization`() {
        val sessionInfo =
            QrCodeSessionInfoDataRequest(
                locale = "en-US",
            )

        val request =
            QrCodePaymentInstrumentDataRequest(
                paymentMethodType = "qrcode",
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
                    "qrcode",
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
    fun `test QrCodeSessionInfoDataRequest serialization`() {
        val sessionInfo =
            QrCodeSessionInfoDataRequest(
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
            JSONSerializationUtils.getJsonObjectSerializer<QrCodeSessionInfoDataRequest>().serialize(sessionInfo)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }
}
