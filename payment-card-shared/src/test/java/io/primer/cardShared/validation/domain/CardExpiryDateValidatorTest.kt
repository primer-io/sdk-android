package io.primer.cardShared.validation.domain

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.error.PrimerInputValidationError
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CardExpiryDateValidatorTest {

    private lateinit var cardExpiryDateValidator: CardExpiryDateValidator

    @BeforeEach
    fun setUp() {
        cardExpiryDateValidator = CardExpiryDateValidator()
    }

    @Test
    fun `validate should return error when expiry date is blank`() = runTest {
        val result = cardExpiryDateValidator.validate("")

        assertEquals(
            PrimerInputValidationError(
                CardExpiryDateValidator.EXPIRY_DATE_INVALID_ERROR_ID,
                "Card expiry date cannot be blank.",
                PrimerInputElementType.EXPIRY_DATE
            ),
            result
        )
    }

    @Test
    fun `validate should return error when expiry date is null`() = runTest {
        val result = cardExpiryDateValidator.validate(null)

        assertEquals(
            PrimerInputValidationError(
                CardExpiryDateValidator.EXPIRY_DATE_INVALID_ERROR_ID,
                "Card expiry date cannot be blank.",
                PrimerInputElementType.EXPIRY_DATE
            ),
            result
        )
    }

    @Test
    fun `validate should return error when expiry date format is invalid`() = runTest {
        val invalidExpiryDates = listOf("13/2024", "00/2024", "10/24", "102024", "10-2024", "abcd")

        invalidExpiryDates.forEach { expiryDate ->
            val result = cardExpiryDateValidator.validate(expiryDate)
            assertEquals(
                PrimerInputValidationError(
                    CardExpiryDateValidator.EXPIRY_DATE_INVALID_ERROR_ID,
                    "Card expiry date is not valid. Valid expiry date format is MM/YYYY.",
                    PrimerInputElementType.EXPIRY_DATE
                ),
                result
            )
        }
    }

    @Test
    fun `validate should return null when expiry date format is valid`() = runTest {
        val validExpiryDates = listOf("01/2027", "12/2030", "10/2025")

        validExpiryDates.forEach { expiryDate ->
            val result = cardExpiryDateValidator.validate(expiryDate)
            assertEquals(null, result)
        }
    }

    @Test
    fun `validate should pad single digit month with leading zero and consider it valid`() = runTest {
        val result = cardExpiryDateValidator.validate("1/2027")

        assertEquals(null, result)
    }

    @Test
    fun `validate should return error when expiry date has invalid year`() = runTest {
        val result = cardExpiryDateValidator.validate("10/24")

        assertEquals(
            PrimerInputValidationError(
                CardExpiryDateValidator.EXPIRY_DATE_INVALID_ERROR_ID,
                "Card expiry date is not valid. Valid expiry date format is MM/YYYY.",
                PrimerInputElementType.EXPIRY_DATE
            ),
            result
        )
    }
}
