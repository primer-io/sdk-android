package io.primer.android.viewmodel

import io.mockk.every
import io.mockk.mockk
import io.primer.android.R
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CardViewModelTest {
    private lateinit var primerCardData: PrimerCardData

    @BeforeEach
    fun setUp() {
        primerCardData = mockk(relaxed = true)
    }

    @Test
    fun `toSyncValidationError maps invalid-card-number correctly when card number is blank`() {
        every { primerCardData.cardNumber } returns ""
        val error = PrimerInputValidationError(
            inputElementType = PrimerInputElementType.CARD_NUMBER,
            description = "",
            errorId = "invalid-card-number"
        )

        val syncError = error.toSyncValidationError(primerCardData)

        assertEquals(PrimerInputElementType.CARD_NUMBER, syncError.inputElementType)
        assertEquals("invalid-card-number", syncError.errorId)
        assertEquals(R.string.form_error_required, syncError.errorFormatId)
        assertEquals(R.string.card_number, syncError.fieldId)
    }

    @Test
    fun `toSyncValidationError maps invalid-card-number correctly when card number is not blank`() {
        every { primerCardData.cardNumber } returns "1234"
        val error = PrimerInputValidationError(
            inputElementType = PrimerInputElementType.CARD_NUMBER,
            description = "",
            errorId = "invalid-card-number"
        )

        val syncError = error.toSyncValidationError(primerCardData)

        assertEquals(PrimerInputElementType.CARD_NUMBER, syncError.inputElementType)
        assertEquals("invalid-card-number", syncError.errorId)
        assertEquals(R.string.form_error_invalid, syncError.errorFormatId)
        assertEquals(R.string.card_number, syncError.fieldId)
    }

    @Test
    fun `toSyncValidationError maps invalid-cvv correctly when cvv is blank`() {
        every { primerCardData.cvv } returns ""
        val error = PrimerInputValidationError(
            inputElementType = PrimerInputElementType.CVV,
            description = "",
            errorId = "invalid-cvv"
        )

        val syncError = error.toSyncValidationError(primerCardData)

        assertEquals(PrimerInputElementType.CVV, syncError.inputElementType)
        assertEquals("invalid-cvv", syncError.errorId)
        assertEquals(R.string.form_error_required, syncError.errorFormatId)
        assertEquals(R.string.card_cvv, syncError.fieldId)
    }

    @Test
    fun `toSyncValidationError maps invalid-cvv correctly when cvv is not blank`() {
        every { primerCardData.cvv } returns "123"
        val error = PrimerInputValidationError(
            inputElementType = PrimerInputElementType.CVV,
            description = "",
            errorId = "invalid-cvv"
        )

        val syncError = error.toSyncValidationError(primerCardData)

        assertEquals(PrimerInputElementType.CVV, syncError.inputElementType)
        assertEquals("invalid-cvv", syncError.errorId)
        assertEquals(R.string.form_error_invalid, syncError.errorFormatId)
        assertEquals(R.string.card_cvv, syncError.fieldId)
    }

    @Test
    fun `toSyncValidationError maps invalid-expiry-date correctly when expiration is blank`() {
        every { primerCardData.expiryDate } returns ""
        val error = PrimerInputValidationError(
            inputElementType = PrimerInputElementType.EXPIRY_DATE,
            description = "",
            errorId = "invalid-expiry-date"
        )

        val syncError = error.toSyncValidationError(primerCardData)

        assertEquals(PrimerInputElementType.EXPIRY_DATE, syncError.inputElementType)
        assertEquals("invalid-expiry-date", syncError.errorId)
        assertEquals(R.string.form_error_required, syncError.errorFormatId)
        assertEquals(R.string.card_expiry, syncError.fieldId)
    }

    @Test
    fun `toSyncValidationError maps invalid-expiry-date correctly when expiration is not blank`() {
        every { primerCardData.expiryDate } returns "13/37"
        val error = PrimerInputValidationError(
            inputElementType = PrimerInputElementType.EXPIRY_DATE,
            description = "",
            errorId = "invalid-expiry-date"
        )

        val syncError = error.toSyncValidationError(primerCardData)

        assertEquals(PrimerInputElementType.EXPIRY_DATE, syncError.inputElementType)
        assertEquals("invalid-expiry-date", syncError.errorId)
        assertEquals(R.string.form_error_invalid, syncError.errorFormatId)
        assertEquals(R.string.card_expiry, syncError.fieldId)
    }

    @Test
    fun `toSyncValidationError maps invalid-cardholder-name correctly when name is blank`() {
        every { primerCardData.cardHolderName } returns ""
        val error = PrimerInputValidationError(
            inputElementType = PrimerInputElementType.CARDHOLDER_NAME,
            description = "",
            errorId = "invalid-cardholder-name"
        )

        val syncError = error.toSyncValidationError(primerCardData)

        assertEquals(PrimerInputElementType.CARDHOLDER_NAME, syncError.inputElementType)
        assertEquals("invalid-cardholder-name", syncError.errorId)
        assertEquals(R.string.form_error_required, syncError.errorFormatId)
        assertEquals(R.string.card_holder_name, syncError.fieldId)
    }

    @Test
    fun `toSyncValidationError maps invalid-cardholder-name correctly when name is too short`() {
        every { primerCardData.cardHolderName } returns "J"
        val error = PrimerInputValidationError(
            inputElementType = PrimerInputElementType.CARDHOLDER_NAME,
            description = "",
            errorId = "invalid-cardholder-name"
        )

        val syncError = error.toSyncValidationError(primerCardData)

        assertEquals(PrimerInputElementType.CARDHOLDER_NAME, syncError.inputElementType)
        assertEquals("invalid-cardholder-name", syncError.errorId)
        assertEquals(R.string.form_error_card_holder_name_length, syncError.errorFormatId)
        assertEquals(R.string.card_holder_name, syncError.fieldId)
    }

    @Test
    fun `toSyncValidationError maps invalid-cardholder-name correctly when name is too long`() {
        every { primerCardData.cardHolderName } returns "J".repeat(46)
        val error = PrimerInputValidationError(
            inputElementType = PrimerInputElementType.CARDHOLDER_NAME,
            description = "",
            errorId = "invalid-cardholder-name"
        )

        val syncError = error.toSyncValidationError(primerCardData)

        assertEquals(PrimerInputElementType.CARDHOLDER_NAME, syncError.inputElementType)
        assertEquals("invalid-cardholder-name", syncError.errorId)
        assertEquals(R.string.form_error_card_holder_name_length, syncError.errorFormatId)
        assertEquals(R.string.card_holder_name, syncError.fieldId)
    }

    @Test
    fun `toSyncValidationError throws exception for unsupported errorId`() {
        val error = PrimerInputValidationError(
            inputElementType = PrimerInputElementType.CARD_NUMBER,
            description = "",
            errorId = "unsupported-error-id"
        )

        assertThrows<IllegalStateException> {
            error.toSyncValidationError(primerCardData)
        }
    }
}
