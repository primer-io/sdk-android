import io.primer.android.ui.ExpiryDateFormatter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Calendar
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpiryDateFormatterTest {
    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    @Test
    fun `When using current month and year, then isValid returns true`() {
        val formatter = ExpiryDateFormatter.fromString("$currentMonth/$currentYear")
        assertEquals(currentMonth, formatter.getMonth().toInt())
        assertEquals(currentYear, formatter.getYear().toInt())
        assertTrue(formatter.isValid())
        assertFalse(formatter.isEmpty())
    }

    @Test
    fun `When using current month and last year, then isValid returns false`() {
        val formatter = ExpiryDateFormatter.fromString("$currentMonth/${currentYear - 1}")
        assertEquals(currentMonth, formatter.getMonth().toInt())
        assertEquals(currentYear - 1, formatter.getYear().toInt())
        assertFalse(formatter.isValid())
        assertFalse(formatter.isEmpty())
    }

    @Test
    fun `When using next month and current year, then isValid returns true`() {
        val nextMonth = currentMonth + 1
        val formatter = ExpiryDateFormatter.fromString("$nextMonth/$currentYear")
        assertEquals(nextMonth, formatter.getMonth().toInt())
        assertEquals(currentYear, formatter.getYear().toInt())
        assertTrue(formatter.isValid())
        assertFalse(formatter.isEmpty())
    }

    @Test
    fun `When using expired date, then isValid returns false`() {
        val formatter = ExpiryDateFormatter.fromString("13/${currentYear - 1}")
        assertEquals(12, formatter.getMonth().toInt())
        assertEquals(currentYear - 1, formatter.getYear().toInt())
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
        val formatter = ExpiryDateFormatter.fromString(currentMonth.toString())
        assertFalse(formatter.isValid())
        assertFalse(formatter.isEmpty())
    }

    @Test
    fun `When the value is just year, then the isEmpty returns false`() {
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
