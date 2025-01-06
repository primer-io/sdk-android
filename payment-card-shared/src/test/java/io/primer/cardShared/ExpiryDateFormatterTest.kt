package io.primer.cardShared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Calendar
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpiryDateFormatterTest {
    @Test
    fun `When using current month and year, then isValid returns true`() {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        val formatter =
            ExpiryDateFormatter.fromString(
                "${month.toString().padStart(2, '0')}/$year",
            )
        assertEquals(month, formatter.getMonth().toInt())
        assertEquals(year, formatter.getYear().toInt())
        assertTrue(formatter.isValid())
        assertFalse(formatter.isEmpty())
    }

    @Test
    fun `When using current month and last year, then isValid returns false`() {
        val dateDecrementedByYear =
            Calendar.getInstance().apply {
                add(Calendar.YEAR, -1)
            }
        val month = dateDecrementedByYear.get(Calendar.MONTH) + 1
        val year = dateDecrementedByYear.get(Calendar.YEAR)
        val formatter =
            ExpiryDateFormatter.fromString(
                "${month.toString().padStart(2, '0')}/$year",
            )
        assertEquals(month, formatter.getMonth().toInt())
        assertEquals(year, formatter.getYear().toInt())
        assertFalse(formatter.isValid())
        assertFalse(formatter.isEmpty())
    }

    @Test
    fun `When using next month, then isValid returns true`() {
        val dateIncrementedByMonth =
            Calendar.getInstance().apply {
                add(Calendar.MONTH, 1)
            }
        val month = dateIncrementedByMonth.get(Calendar.MONTH) + 1
        val year = dateIncrementedByMonth.get(Calendar.YEAR)
        val formatter =
            ExpiryDateFormatter.fromString(
                "${month.toString().padStart(2, '0')}/$year",
            )

        assertEquals(month, formatter.getMonth().toInt())
        assertEquals(year, formatter.getYear().toInt())
        assertTrue(formatter.isValid())
        assertFalse(formatter.isEmpty())
    }

    @Test
    fun `When using expired date, then isValid returns false`() {
        val dateDecrementedByMonth =
            Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
            }
        val month = dateDecrementedByMonth.get(Calendar.MONTH) + 1
        val year = dateDecrementedByMonth.get(Calendar.YEAR)
        val formatter =
            ExpiryDateFormatter.fromString(
                "${month.toString().padStart(2, '0')}/$year",
            )
        assertEquals(month, formatter.getMonth().toInt())
        assertEquals(year, formatter.getYear().toInt())
        assertFalse(formatter.isValid())
        assertFalse(formatter.isEmpty())
    }

    @Test
    fun `When the value is not a date format, then the isEmpty returns false`() {
        val formatter = ExpiryDateFormatter.fromString("123")
        assertFalse(formatter.isValid())
        assertFalse(formatter.isEmpty())
    }

    @Test
    fun `When the value is random text, then the isEmpty returns false`() {
        val formatter = ExpiryDateFormatter.fromString("abc")
        assertFalse(formatter.isValid())
        assertTrue(formatter.isEmpty())
    }

    @Test
    fun `When the value is just month, then the isEmpty returns false`() {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val formatter = ExpiryDateFormatter.fromString(currentMonth.toString())
        assertFalse(formatter.isValid())
        assertFalse(formatter.isEmpty())
    }

    @Test
    fun `When the value is just year, then the isEmpty returns false`() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val formatter = ExpiryDateFormatter.fromString(currentYear.toString())
        assertFalse(formatter.isValid())
        assertFalse(formatter.isEmpty())
    }

    @Test
    fun `When the value is empty, then the isEmpty returns true`() {
        val formatter = ExpiryDateFormatter.fromString("")
        assertFalse(formatter.isValid())
        assertTrue(formatter.isEmpty())
    }
}
