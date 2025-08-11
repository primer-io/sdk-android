package io.primer.sample.demos

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.primer.android.PrimerTheme
import io.primer.android.scope.PrimerCheckoutScope

sealed class CheckoutDemo(
    val title: String,
    val description: String,
    val customizationLevel: Int,
    val theme: PrimerTheme = PrimerTheme(),
    val render: @Composable PrimerCheckoutScope.() -> Unit
) {
    companion object {

        fun CheckoutDemo.getBackground(): Color =
            when (customizationLevel) {
                0, 1 -> Color(0xFF8BC34A)
                2 -> Color(0xFFFFEB3B)
                3 -> Color(0xFFFFC107)
                4 -> Color(0xFFFF9800)
                5 -> Color(0xFFFF5722)
                else -> Color.Black
            }
    }
}
