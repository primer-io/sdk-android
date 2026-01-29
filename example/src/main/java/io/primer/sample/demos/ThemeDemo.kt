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
import io.primer.android.internal.tokens.SizeTokens
import io.primer.android.internal.tokens.TypographyTokens
import io.primer.sample.R as DemoR

// ==================== Theme Definitions ====================

private val redTheme = PrimerTheme(
    lightColorTokens = object : LightColorTokens() {
        override val primerColorBrand = Color(0xFFE53E3E)
        override val primerColorBorderOutlinedFocus = Color(0xFFE53E3E)
        override val primerColorBorderOutlinedDefault = Color(0xFFE53E3E)
    },
)

private val greenTheme = PrimerTheme(
    lightColorTokens = object : LightColorTokens() {
        override val primerColorBrand = Color(0xFF38A169)
        override val primerColorBorderOutlinedFocus = Color(0xFF38A169)
        override val primerColorBorderOutlinedDefault = Color(0xFF38A169)
    },
)

private val purpleTheme = PrimerTheme(
    lightColorTokens = object : LightColorTokens() {
        override val primerColorBrand = Color(0xFF805AD5)
        override val primerColorBorderOutlinedFocus = Color(0xFF805AD5)
        override val primerColorBorderOutlinedDefault = Color(0xFF805AD5)
    },
)

private val noRadiusTheme = PrimerTheme(
    radiusTokens = RadiusTokens(
        medium = 0.dp,
        small = 0.dp,
        large = 0.dp,
        xsmall = 0.dp,
        base = 0.dp,
    ),
)

private val smallSizesTheme = PrimerTheme(
    sizeTokens = SizeTokens(
        small = 12.dp,
        medium = 16.dp,
        large = 18.dp,
        xlarge = 22.dp,
        xxlarge = 28.dp,
        xxxlarge = 34.dp,
    ),
)

private val largeSizesTheme = PrimerTheme(
    sizeTokens = SizeTokens(
        small = 24.dp,
        medium = 32.dp,
        large = 40.dp,
        xlarge = 48.dp,
        xxlarge = 64.dp,
        xxxlarge = 80.dp,
    ),
)

private val lightTypographyTheme = with(TypographyTokens()) {
    PrimerTheme(
        typographyTokens = TypographyTokens(
            titleXlarge = titleXlarge.copy(weight = 300),
            titleLarge = titleLarge.copy(weight = 300),
            bodyLarge = bodyLarge.copy(weight = 300),
            bodyMedium = bodyMedium.copy(weight = 300),
            bodySmall = bodySmall.copy(weight = 300),
        ),
    )
}

private val boldTypographyTheme = with(TypographyTokens()) {
    PrimerTheme(
        typographyTokens = TypographyTokens(
            titleXlarge = titleXlarge.copy(weight = 700),
            titleLarge = titleLarge.copy(weight = 700),
            bodyLarge = bodyLarge.copy(weight = 700),
            bodyMedium = bodyMedium.copy(weight = 700),
            bodySmall = bodySmall.copy(weight = 700),
        ),
    )
}

private val largeTypographyTheme = with(TypographyTokens()) {
    PrimerTheme(
        typographyTokens = TypographyTokens(
            titleXlarge = titleXlarge.copy(size = 32),
            titleLarge = titleLarge.copy(size = 32),
            bodyLarge = bodyLarge.copy(size = 32),
            bodyMedium = bodyMedium.copy(size = 32),
            bodySmall = bodySmall.copy(size = 32),
        ),
    )
}

private val customFontTheme = with(TypographyTokens()) {
    PrimerTheme(
        typographyTokens = TypographyTokens(
            titleXlarge = titleXlarge.copy(font = DemoR.font.comic_sans),
            titleLarge = titleLarge.copy(font = DemoR.font.comic_sans),
            bodyLarge = bodyLarge.copy(font = DemoR.font.comic_sans),
            bodyMedium = bodyMedium.copy(font = DemoR.font.comic_sans),
            bodySmall = bodySmall.copy(font = DemoR.font.comic_sans),
        ),
    )
}

// ==================== V2 API Theme Demos ====================

/**
 * Red Brand Theme Demo - V2 API
 *
 * Shows a red brand color with custom accents.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun RedThemeDemoV2(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    PrimerCheckoutSheet(
        checkout = rememberPrimerCheckoutController(clientToken, settings),
        onDismiss = onDismiss,
        theme = redTheme,
    )
}

/**
 * Green Nature Theme Demo - V2 API
 *
 * Shows a green brand with nature-inspired colors.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun GreenThemeDemoV2(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    PrimerCheckoutSheet(
        checkout = rememberPrimerCheckoutController(clientToken, settings),
        onDismiss = onDismiss,
        theme = greenTheme,
    )
}

/**
 * Purple Creative Theme Demo - V2 API
 *
 * Shows a purple brand with creative vibes.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun PurpleThemeDemoV2(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    PrimerCheckoutSheet(
        checkout = rememberPrimerCheckoutController(clientToken, settings),
        onDismiss = onDismiss,
        theme = purpleTheme,
    )
}

/**
 * No Radius Theme Demo - V2 API
 *
 * Shows a sharp, rectangular design with zero border radius.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun NoRadiusThemeDemoV2(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    PrimerCheckoutSheet(
        checkout = rememberPrimerCheckoutController(clientToken, settings),
        onDismiss = onDismiss,
        theme = noRadiusTheme,
    )
}

/**
 * Small Sizes Theme Demo - V2 API
 *
 * Shows a compact design with smaller component sizes.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun SmallSizesThemeDemoV2(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    PrimerCheckoutSheet(
        checkout = rememberPrimerCheckoutController(clientToken, settings),
        onDismiss = onDismiss,
        theme = smallSizesTheme,
    )
}

/**
 * Large Sizes Theme Demo - V2 API
 *
 * Shows a bold design with larger component sizes.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun LargeSizesThemeDemoV2(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    PrimerCheckoutSheet(
        checkout = rememberPrimerCheckoutController(clientToken, settings),
        onDismiss = onDismiss,
        theme = largeSizesTheme,
    )
}

/**
 * Light Typography Theme Demo - V2 API
 *
 * Shows clean, minimal typography with light font weights.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun LightTypographyThemeDemoV2(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    PrimerCheckoutSheet(
        checkout = rememberPrimerCheckoutController(clientToken, settings),
        onDismiss = onDismiss,
        theme = lightTypographyTheme,
    )
}

/**
 * Bold Typography Theme Demo - V2 API
 *
 * Shows strong, impactful typography with bold font weights.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun BoldTypographyThemeDemoV2(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    PrimerCheckoutSheet(
        checkout = rememberPrimerCheckoutController(clientToken, settings),
        onDismiss = onDismiss,
        theme = boldTypographyTheme,
    )
}

/**
 * Large Typography Theme Demo - V2 API
 *
 * Shows accessibility-focused design with large text sizes.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun LargeTypographyThemeDemoV2(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    PrimerCheckoutSheet(
        checkout = rememberPrimerCheckoutController(clientToken, settings),
        onDismiss = onDismiss,
        theme = largeTypographyTheme,
    )
}

/**
 * Custom Font Theme Demo - V2 API
 *
 * Shows custom font resource usage in the checkout flow.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun CustomFontThemeDemoV2(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    PrimerCheckoutSheet(
        checkout = rememberPrimerCheckoutController(clientToken, settings),
        onDismiss = onDismiss,
        theme = customFontTheme,
    )
}
