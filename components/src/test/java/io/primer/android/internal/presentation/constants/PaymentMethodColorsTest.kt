package io.primer.android.internal.presentation.constants

import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaymentMethodColorsTest {

    @Test
    fun `idealPink returns correct color`() {
        val expectedColor = Color(0xFFCC0066)
        assertEquals(expectedColor, PaymentMethodColors.idealPink)
    }

    @Test
    fun `paypalYellow returns correct color`() {
        val expectedColor = Color(0xFFFFC439)
        assertEquals(expectedColor, PaymentMethodColors.paypalYellow)
    }

    @Test
    fun `klarnaPink returns correct color`() {
        val expectedColor = Color(0xFFFFA8CD)
        assertEquals(expectedColor, PaymentMethodColors.klarnaPink)
    }
}
