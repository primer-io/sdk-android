package io.primer.cardShared.validation.domain

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.error.PrimerInputValidationError
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CardholderNameValidatorTest {

    private lateinit var cardholderNameValidator: CardholderNameValidator

    @BeforeEach
    fun setUp() {
        cardholderNameValidator = CardholderNameValidator()
    }

    @Test
    fun `validate should return error when cardholder name is blank`() = runTest {
        val result = cardholderNameValidator.validate("")

        assertEquals(
            PrimerInputValidationError(
                "invalid-cardholder-name",
                "Cardholder name cannot be blank.",
                PrimerInputElementType.CARDHOLDER_NAME
            ),
            result
        )
    }

    @Test
    fun `validate should return error when cardholder name is null`() = runTest {
        val result = cardholderNameValidator.validate(null)

        assertEquals(
            PrimerInputValidationError(
                "invalid-cardholder-name",
                "Cardholder name cannot be blank.",
                PrimerInputElementType.CARDHOLDER_NAME
            ),
            result
        )
    }

    @Test
    fun `validate should return null when cardholder name is valid`() = runTest {
        val result = cardholderNameValidator.validate("John Doe")

        assertEquals(null, result)
    }
}
