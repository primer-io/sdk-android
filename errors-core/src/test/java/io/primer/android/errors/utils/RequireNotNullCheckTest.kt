package io.primer.android.errors.utils

import io.mockk.every
import io.mockk.mockk
import io.primer.android.errors.data.exception.IllegalValueKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RequireNotNullCheckTest {

    @Test
    fun `requireNotNullCheck should return value when not null`() {
        // Arrange
        val value = "Test"
        val key = mockk<IllegalValueKey> {
            every { key } returns "test_key"
        }

        // Act
        val result = requireNotNullCheck(value, key)

        // Assert
        assertEquals(value, result)
    }

    @Test
    fun `requireNotNullCheck should throw IllegalValueException when value is null`() {
        // Arrange
        val value: String? = null
        val key = mockk<IllegalValueKey> {
            every { key } returns "test_key"
        }
        val expectedMessage = "Required value for test_key was null."

        // Act & Assert
        val exception = assertThrows(io.primer.android.errors.data.exception.IllegalValueException::class.java) {
            requireNotNullCheck(value, key)
        }
        assertEquals(key, exception.key)
        assertEquals(expectedMessage, exception.message)
    }
}
