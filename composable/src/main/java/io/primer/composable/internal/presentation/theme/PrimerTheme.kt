package io.primer.composable.internal.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import io.primer.composable.internal.tokens.DarkColorTokens
import io.primer.composable.internal.tokens.LightColorTokens
import io.primer.composable.internal.tokens.RadiusTokens
import io.primer.composable.internal.tokens.SizeTokens
import io.primer.composable.internal.tokens.SpacingTokens
import io.primer.composable.internal.tokens.TypographyTokens

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

val LocalPrimerTypographyTokens = staticCompositionLocalOf<TypographyTokens> {
    error("No PrimerTypographyTokens provided")
}

// TODO COMPOSABLE optimise this
@Composable
private fun createColorScheme(colorTokens: LightColorTokens): ColorScheme {
    return if (colorTokens is DarkColorTokens) {
        darkColorScheme(
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
    } else {
        lightColorScheme(
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
    }
}

@Composable
internal fun PrimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorTokens = if (darkTheme) DarkColorTokens() else LightColorTokens()

    val colorScheme = createColorScheme(colorTokens)

    CompositionLocalProvider(
        LocalPrimerColorTokens provides colorTokens,
        LocalPrimerSizeTokens provides SizeTokens(),
        LocalPrimerSpacingTokens provides SpacingTokens(),
        LocalPrimerRadiusTokens provides RadiusTokens(),
        LocalPrimerTypographyTokens provides TypographyTokens(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
