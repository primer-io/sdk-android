package io.primer.android.nolpay.implementation.paymentCard.tokenization.data.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import org.json.JSONObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class NolPayPaymentInstrumentDataRequestTest {

    @Test
    fun `serializer should correctly serialize NolPayPaymentInstrumentDataRequest to JSONObject`() {
        val sessionInfo = NolPaySessionInfoDataRequest(
            mobileCountryCode = "US",
            mobileNumber = "1234567890",
            nolPayCardNumber = "1234567812345678",
            deviceVendor = "TestManufacturer",
            deviceModel = "TestModel"
        )

        val request = NolPayPaymentInstrumentDataRequest(
            paymentMethodType = "CARD",
            paymentMethodConfigId = "configId",
            sessionInfo = sessionInfo,
            type = PaymentInstrumentType.OFF_SESSION_PAYMENT
        )

        val expectedJson = JSONObject().apply {
            put("type", PaymentInstrumentType.OFF_SESSION_PAYMENT.name)
            put("paymentMethodType", "CARD")
            put("paymentMethodConfigId", "configId")
            put(
                "sessionInfo",
                JSONObject().apply {
                    put(
                        "platform",
                        "ANDROID"
                    )
                    put(
                        "mobileCountryCode",
                        "US"
                    )
                    put(
                        "mobileNumber",
                        "1234567890"
                    )
                    put(
                        "nolPayCardNumber",
                        "1234567812345678"
                    )
                    put(
                        "phoneVendor",
                        "TestManufacturer"
                    )
                    put(
                        "phoneModel",
                        "TestModel"
                    )
                }
            )
        }

        val serializedJson = NolPayPaymentInstrumentDataRequest.serializer.serialize(request)

        assertEquals(expectedJson.toString(), serializedJson.toString())
    }
}
