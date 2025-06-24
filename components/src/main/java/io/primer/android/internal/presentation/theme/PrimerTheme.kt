package io.primer.android.internal.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import io.primer.android.internal.tokens.DarkColorTokens
import io.primer.android.internal.tokens.LightColorTokens
import io.primer.android.internal.tokens.RadiusTokens
import io.primer.android.internal.tokens.SizeTokens
import io.primer.android.internal.tokens.SpacingTokens
import io.primer.android.internal.tokens.TypographyTokens

val LocalPrimerColorTokens = staticCompositionLocalOf<LightColorTokens> {
    error("No PrimerColorTokens provided")
}

val LocalPrimerRadiusTokens = staticCompositionLocalOf<RadiusTokens> {
    error("No PrimerRadiusTokens provided")
}

val LocalPrimerSizeTokens = staticCompositionLocalOf<SizeTokens> {
    error("No PrimerSizeTokens provided")
}

val LocalPrimerSpacingTokens = staticCompositionLocalOf<SpacingTokens> {
    error("No PrimerSpacingTokens provided")
}

//TODO handle font weight correctly
val LocalPrimerTypographyTokens = staticCompositionLocalOf<TypographyTokens> {
    error("No PrimerTypographyTokens provided")
}

private fun createDarkColorScheme(colorTokens: DarkColorTokens) = darkColorScheme(
    primary = colorTokens.primerColorBrand,
    onPrimary = colorTokens.primerColorGray000,
    secondary = colorTokens.primerColorGray600,
    onSecondary = colorTokens.primerColorGray100,
    tertiary = colorTokens.primerColorBlue500,
    onTertiary = colorTokens.primerColorGray000,
    background = colorTokens.primerColorGray000,
    onBackground = colorTokens.primerColorGray900,
    surface = colorTokens.primerColorGray100,
    onSurface = colorTokens.primerColorGray900,
    surfaceVariant = colorTokens.primerColorGray200,
    onSurfaceVariant = colorTokens.primerColorGray600,
    outline = colorTokens.primerColorGray400,
    outlineVariant = colorTokens.primerColorGray300,
    error = colorTokens.primerColorRed500,
    onError = colorTokens.primerColorGray000,
    errorContainer = colorTokens.primerColorRed100,
    onErrorContainer = colorTokens.primerColorRed900,
)

private fun createLightColorScheme(colorTokens: LightColorTokens) = lightColorScheme(
    primary = colorTokens.primerColorBrand,
    onPrimary = colorTokens.primerColorGray000,
    secondary = colorTokens.primerColorGray600,
    onSecondary = colorTokens.primerColorGray000,
    tertiary = colorTokens.primerColorBlue500,
    onTertiary = colorTokens.primerColorGray000,
    background = colorTokens.primerColorBackground,
    onBackground = colorTokens.primerColorTextPrimary,
    surface = colorTokens.primerColorGray000,
    onSurface = colorTokens.primerColorTextPrimary,
    surfaceVariant = colorTokens.primerColorGray100,
    onSurfaceVariant = colorTokens.primerColorGray600,
    outline = colorTokens.primerColorBorderOutlinedDefault,
    outlineVariant = colorTokens.primerColorGray300,
    error = colorTokens.primerColorRed500,
    onError = colorTokens.primerColorGray000,
    errorContainer = colorTokens.primerColorRed100,
    onErrorContainer = colorTokens.primerColorRed900,
)

@Composable
internal fun PrimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val (colorTokens, colorScheme) = remember(darkTheme) {
        if (darkTheme) {
            val tokens = DarkColorTokens()
            tokens to createDarkColorScheme(tokens)
        } else {
            val tokens = LightColorTokens()
            tokens to createLightColorScheme(tokens)
        }
    }

    val sizeTokens = remember { SizeTokens() }
    val spacingTokens = remember { SpacingTokens() }
    val radiusTokens = remember { RadiusTokens() }
    val typographyTokens = remember { TypographyTokens() }

    CompositionLocalProvider(
        LocalPrimerColorTokens provides colorTokens,
        LocalPrimerSizeTokens provides sizeTokens,
        LocalPrimerSpacingTokens provides spacingTokens,
        LocalPrimerRadiusTokens provides radiusTokens,
        LocalPrimerTypographyTokens provides typographyTokens,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
