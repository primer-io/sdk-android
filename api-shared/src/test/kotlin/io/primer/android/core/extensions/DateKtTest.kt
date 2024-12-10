package io.primer.android.core.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Calendar
import java.util.TimeZone

class DateKtTest {
    @Test
    fun `toIso8601String() should return date in the correct format`() {
        val date = Calendar.getInstance().apply {
            timeInMillis = 1713505621000
            timeZone = TimeZone.getTimeZone("UTC")
        }.time

        val result = date.toIso8601String()

        assertEquals(
            "2024-04-19T05:47:01.000Z",
            result
        )
    }
}
