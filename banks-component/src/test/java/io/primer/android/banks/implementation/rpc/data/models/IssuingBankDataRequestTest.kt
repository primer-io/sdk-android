package io.primer.android.banks.implementation.rpc.data.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IssuingBankDataRequestTest {
    @Test
    fun `test serializer serializes correctly`() {
        // Arrange
        val parameters = IssuingBankDataParameters("visa", "en_US")
        val request =
            IssuingBankDataRequest(
                paymentMethodConfigId = "12345",
                command = "FETCH_BANK_ISSUERS",
                parameters = parameters,
            )

        // Act
        val json = IssuingBankDataRequest.serializer.serialize(request)

        // Assert
        assertEquals("12345", json.getString(IssuingBankDataRequest.PAYMENT_METHOD_CONFIG_ID_FIELD))
        assertEquals("FETCH_BANK_ISSUERS", json.getString(IssuingBankDataRequest.COMMAND_FIELD))

        val parametersJson = json.getJSONObject(IssuingBankDataRequest.PARAMETERS_FIELD)
        assertEquals("visa", parametersJson.getString(IssuingBankDataParameters.PAYMENT_METHOD_FIELD))
        assertEquals("en_US", parametersJson.getString(IssuingBankDataParameters.LOCALE_FIELD))
    }
}
