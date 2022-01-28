package io.primer.android.domain.payments.forms.validation.iban

import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class IBANChecksumValidatorTest {

    private lateinit var validator: IBANChecksumValidator

    @BeforeEach
    fun setUp() {
        validator = IBANChecksumValidator()
    }

    @Test
    fun `validate should return false when IBAN is null`() {
        assertFalse(validator.validate(null))
    }

    @Test
    fun `validate should return false when IBAN is blank`() {
        assertFalse(validator.validate(" "))
    }

    @Test
    fun `validate should return false when IBAN is less than 5 chars`() {
        assertFalse(validator.validate("DE13"))
    }

    @Test
    fun `validate should return true when NL IBAN is correct`() {
        assertTrue(validator.validate("NL13TEST0123456789"))
    }

    @Test
    fun `validate should return fals when NL IBAN is incorrect`() {
        assertFalse(validator.validate("NL13TEST0123456k89"))
    }

    @Test
    fun `validate should return true when DE IBAN is correct`() {
        assertTrue(validator.validate("DE41444488889876543210"))
    }

    @Test
    fun `validate should return false when DE IBAN is incorrect`() {
        assertFalse(validator.validate("DE41444488889876543214"))
    }

    @Test
    fun `validate should return true when IT IBAN is correct`() {
        assertTrue(validator.validate("IT60X0542811101000000123456"))
    }

    @Test
    fun `validate should return false when IT IBAN is incorrect`() {
        assertFalse(validator.validate("IT60X0542811101000000123453"))
    }

    @Test
    fun `validate should return true when FR IBAN is correct`() {
        assertTrue(validator.validate("FR1420041010050500013M02606"))
    }

    @Test
    fun `validate should return false when FR IBAN is incorrect`() {
        assertFalse(validator.validate("FR1420041010050500013M02604"))
    }

    @Test
    fun `validate should return true when ES IBAN is correct`() {
        assertTrue(validator.validate("ES9121000418450200051332"))
    }

    @Test
    fun `validate should return false when ES IBAN is incorrect`() {
        assertFalse(validator.validate("ES9121000418450200051334"))
    }

    @Test
    fun `validate should return true when AT IBAN is correct`() {
        assertTrue(validator.validate("AT151234512345678901"))
    }

    @Test
    fun `validate should return false when AT IBAN is incorrect`() {
        assertFalse(validator.validate("AT151234512345678904"))
    }

    @Test
    fun `validate should return true when CH IBAN is correct`() {
        assertTrue(validator.validate("CH4912345123456789012"))
    }

    @Test
    fun `validate should return false when CH IBAN is incorrect`() {
        assertFalse(validator.validate("CH4912345123456789014"))
    }

    @Test
    fun `validate should return true when DK IBAN is correct`() {
        assertTrue(validator.validate("DK8612341234567890"))
    }

    @Test
    fun `validate should return false when DK IBAN is incorrect`() {
        assertFalse(validator.validate("DK8612341234567895"))
    }

    @Test
    fun `validate should return true when NO IBAN is correct`() {
        assertTrue(validator.validate("NO6012341234561"))
    }

    @Test
    fun `validate should return false when NO IBAN is incorrect`() {
        assertFalse(validator.validate("NO6012341234563"))
    }

    @Test
    fun `validate should return true when PL IBAN is correct`() {
        assertTrue(validator.validate("PL20123123411234567890123456"))
    }

    @Test
    fun `validate should return false when PL IBAN is incorrect`() {
        assertFalse(validator.validate("PL20123123411234567890123454"))
    }

    @Test
    fun `validate should return true when SE IBAN is correct`() {
        assertTrue(validator.validate("SE9412312345678901234561"))
    }

    @Test
    fun `validate should return false when SE IBAN is incorrect`() {
        assertFalse(validator.validate("SE9412312345678903234561"))
    }
}
