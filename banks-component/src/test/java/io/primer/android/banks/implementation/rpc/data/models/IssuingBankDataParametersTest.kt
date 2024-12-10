package io.primer.android.banks.implementation.rpc.data.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IssuingBankDataParametersTest {

    @Test
    fun `test serializer serializes correctly`() {
        // Arrange
        val parameters = IssuingBankDataParameters("visa", "en_US")

        // Act
        val json = IssuingBankDataParameters.serializer.serialize(parameters)

        // Assert
        assertEquals("visa", json.getString(IssuingBankDataParameters.PAYMENT_METHOD_FIELD))
        assertEquals("en_US", json.getString(IssuingBankDataParameters.LOCALE_FIELD))
    }
}
