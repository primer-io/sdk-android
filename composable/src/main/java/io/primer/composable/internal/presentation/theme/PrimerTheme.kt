package io.primer.composable.internal.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
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
    RadiusTokens()
}

val LocalPrimerSizeTokens = staticCompositionLocalOf<SizeTokens> {
    SizeTokens()
}

val LocalPrimerSpacingTokens = staticCompositionLocalOf<SpacingTokens> {
    SpacingTokens()
}

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
            onErrorContainer = colorTokens.primerColorRed900
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
            onErrorContainer = colorTokens.primerColorRed900
        )
    }
}

@Composable
private fun createTypography(typographyTokens: TypographyTokens): Typography {
    return Typography(
        displayLarge = typographyTokens.titleXlarge.toTextStyle(),
        headlineMedium = typographyTokens.titleLarge.toTextStyle(),
        titleLarge = typographyTokens.titleLarge.toTextStyle(),
        bodyLarge = typographyTokens.bodyLarge.toTextStyle(),
        bodyMedium = typographyTokens.bodyMedium.toTextStyle(),
        bodySmall = typographyTokens.bodySmall.toTextStyle(),
        labelMedium = typographyTokens.bodyMedium.toTextStyle(),
        labelSmall = typographyTokens.bodySmall.toTextStyle()
    )
}

@Composable
internal fun PrimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorTokens = if (darkTheme) DarkColorTokens() else LightColorTokens()
    val typographyTokens = TypographyTokens()
    val sizeTokens = SizeTokens()
    val spacingTokens = SpacingTokens()
    val radiusTokens = RadiusTokens()
    
    val colorScheme = createColorScheme(colorTokens)
    val typography = createTypography(typographyTokens)
    
    CompositionLocalProvider(
        LocalPrimerColorTokens provides colorTokens,
        LocalPrimerSizeTokens provides sizeTokens,
        LocalPrimerSpacingTokens provides spacingTokens,
        LocalPrimerRadiusTokens provides radiusTokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
