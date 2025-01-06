package io.primer.android.phoneNumber.implementation.tokenization.data.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.phoneNumber.implementation.tokenization.data.model.PhoneNumberPaymentInstrumentDataRequest.Companion.serializer
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PhoneNumberPaymentInstrumentDataRequestTest {
    @Test
    fun `test PhoneNumberPaymentInstrumentDataRequest serialization`() {
        val sessionInfo =
            PhoneNumberSessionInfoDataRequest(
                locale = "en-US",
                phoneNumber = "1234567890",
            )

        val request =
            PhoneNumberPaymentInstrumentDataRequest(
                paymentMethodType = "phoneNumber",
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
                    "phoneNumber",
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
                        put(
                            "phoneNumber",
                            "1234567890",
                        )
                    },
                )
            }

        val serializedJson = serializer.serialize(request)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }

    @Test
    fun `test PhoneNumberSessionInfoDataRequest serialization`() {
        val sessionInfo =
            PhoneNumberSessionInfoDataRequest(
                locale = "en-US",
                phoneNumber = "1234567890",
            )

        val expectedJson =
            JSONObject().apply {
                put(
                    "locale",
                    "en-US",
                )
                put(
                    "phoneNumber",
                    "1234567890",
                )
                put(
                    "platform",
                    "ANDROID",
                )
            }

        val serializedJson =
            JSONSerializationUtils.getJsonObjectSerializer<PhoneNumberSessionInfoDataRequest>().serialize(sessionInfo)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }
}
