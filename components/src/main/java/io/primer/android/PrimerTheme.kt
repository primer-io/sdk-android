package io.primer.android

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.primer.android.internal.tokens.BorderWidthTokens
import io.primer.android.internal.tokens.DarkColorTokens
import io.primer.android.internal.tokens.LightColorTokens
import io.primer.android.internal.tokens.RadiusTokens
import io.primer.android.internal.tokens.SizeTokens
import io.primer.android.internal.tokens.SpacingTokens
import io.primer.android.internal.tokens.TypographyTokens

data class PrimerTheme(
    val lightColorTokens: LightColorTokens = LightColorTokens(),
    val darkColorTokens: DarkColorTokens = DarkColorTokens(),
    val borderWidthTokens: BorderWidthTokens = BorderWidthTokens(),
    val radiusTokens: RadiusTokens = RadiusTokens(),
    val sizeTokens: SizeTokens = SizeTokens(),
    val spacingTokens: SpacingTokens = SpacingTokens(),
    val typographyTokens: TypographyTokens = TypographyTokens(),
) {
    @Composable
    fun colorTokens(
        darkTheme: Boolean = isSystemInDarkTheme(),
    ): LightColorTokens {
        return if (darkTheme) {
            darkColorTokens
        } else {
            lightColorTokens
        }
    }
}

val LocalPrimerTheme = staticCompositionLocalOf { PrimerTheme() }

/**
 * Creates Material 3 ColorScheme from Primer color tokens.
 *
 * Works for both LightColorTokens and DarkColorTokens (which extends Light).
 * Detects theme mode using type checking for DarkColorTokens.
 *
 * Key challenge: Gray tokens flip between modes
 * - Light: gray.000 = white, gray.900 = dark
 * - Dark:  gray.000 = dark, gray.900 = light
 *
 * For "on" colors on colored surfaces, we need light text in BOTH modes:
 * - Light: use gray.000 (white)
 * - Dark:  use gray.900 (light)
 */
private fun LightColorTokens.toMaterialColorScheme(): ColorScheme {
    // Detect mode using type check
    val isLightMode = this !is DarkColorTokens

    // For colored surfaces, select the correct "light" token for each mode
    val onColoredSurface = if (isLightMode) primerColorGray000 else primerColorGray900

    return ColorScheme(
        primary = primerColorBrand,
        onPrimary = onColoredSurface,
        primaryContainer = primerColorBlue900,
        onPrimaryContainer = onColoredSurface,
        inversePrimary = primerColorBlue500,
        secondary = primerColorGray600,
        onSecondary = onColoredSurface,
        secondaryContainer = primerColorGray200,
        onSecondaryContainer = primerColorTextPrimary,
        tertiary = primerColorBlue500,
        onTertiary = onColoredSurface,
        tertiaryContainer = primerColorGray200,
        onTertiaryContainer = primerColorTextPrimary,
        background = primerColorBackground,
        onBackground = primerColorTextPrimary,
        surface = primerColorBackground,
        onSurface = primerColorTextPrimary,
        surfaceVariant = primerColorGray100,
        onSurfaceVariant = primerColorTextSecondary,
        surfaceTint = primerColorBrand,
        inverseSurface = primerColorGray900,
        inverseOnSurface = primerColorGray000,
        error = primerColorRed500,
        onError = onColoredSurface,
        errorContainer = primerColorRed100,
        onErrorContainer = primerColorTextNegative,
        outline = primerColorBorderOutlinedDefault,
        outlineVariant = primerColorGray300,
        scrim = Color.Black.copy(alpha = 0.32f),
        // Surface elevation: lighter = higher (but tokens flip in dark mode)
        surfaceBright = if (isLightMode) primerColorGray000 else primerColorGray300,
        surfaceDim = if (isLightMode) primerColorGray100 else primerColorGray000,
        surfaceContainer = primerColorGray100,
        surfaceContainerHigh = primerColorGray200,
        surfaceContainerHighest = primerColorGray300,
        surfaceContainerLow = primerColorGray100,
        surfaceContainerLowest = primerColorBackground,
    )
}

@Composable
internal fun PrimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val theme = LocalPrimerTheme.current
    val colorTokens = if (darkTheme) theme.darkColorTokens else theme.lightColorTokens

    MaterialTheme(
        colorScheme = colorTokens.toMaterialColorScheme(),
        content = content,
    )
}
