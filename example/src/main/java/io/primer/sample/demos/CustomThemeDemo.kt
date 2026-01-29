package io.primer.sample.demos

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.primer.android.PrimerTheme
import io.primer.android.api.checkout.PrimerCheckoutSheet
import io.primer.android.api.checkout.rememberPrimerCheckoutController
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.internal.tokens.LightColorTokens
import io.primer.android.internal.tokens.RadiusTokens
import io.primer.android.internal.tokens.TypographyTokens

/**
 * Demo: Custom Theme
 *
 * Shows how to customize the checkout appearance using PrimerTheme.
 * Demonstrates:
 * - Custom brand color (purple)
 * - Custom border/focus colors
 * - Rounded corners
 * - Custom typography weights
 *
 * Uses V2 API with custom theme passed to PrimerCheckoutSheet.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun CustomThemeDemo(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    // Custom purple theme with rounded corners
    val customTheme = PrimerTheme(
        lightColorTokens = object : LightColorTokens() {
            override val primerColorBrand = Color(0xFF6B46C1)  // Purple
            override val primerColorBorderOutlinedFocus = Color(0xFF6B46C1)
            override val primerColorBorderOutlinedDefault = Color(0xFFD6BCFA)  // Light purple
        },
        radiusTokens = RadiusTokens(
            xsmall = 8.dp,
            small = 12.dp,
            medium = 16.dp,
            large = 24.dp,
            base = 12.dp,
        ),
        typographyTokens = with(TypographyTokens()) {
            TypographyTokens(
                titleXlarge = titleXlarge.copy(weight = 600),
                titleLarge = titleLarge.copy(weight = 600),
                bodyLarge = bodyLarge.copy(weight = 400),
                bodyMedium = bodyMedium.copy(weight = 400),
                bodySmall = bodySmall.copy(weight = 400),
            )
        },
    )

    PrimerCheckoutSheet(
        checkout = checkout,
        onDismiss = onDismiss,
        theme = customTheme,
    )
}
