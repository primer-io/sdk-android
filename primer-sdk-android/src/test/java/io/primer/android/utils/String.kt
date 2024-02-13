package io.primer.android.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class String {

    @Test
    fun `sanitizedCardNumber should remove non-numeric characters`() {
        // Given
        val originalCardNumber = "1234-5678-90ab-cdef 0 @ 0"

        // When
        val sanitizedNumber = originalCardNumber.sanitizedCardNumber()

        // Then
        assertEquals("123456789000", sanitizedNumber)
    }

    @Test
    fun `sanitizedCardNumber should handle an already sanitized number`() {
        // Given
        val sanitizedCardNumber = "9876543210"

        // When
        val result = sanitizedCardNumber.sanitizedCardNumber()

        // Then
        assertEquals(sanitizedCardNumber, result)
    }

    @Test
    fun `sanitizedCardNumber should handle an empty string`() {
        // Given
        val emptyString = ""

        // When
        val result = emptyString.sanitizedCardNumber()

        // Then
        assertEquals(emptyString, result)
    }
}
