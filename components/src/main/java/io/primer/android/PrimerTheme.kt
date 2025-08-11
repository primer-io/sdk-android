package io.primer.android

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import io.primer.android.internal.tokens.DarkColorTokens
import io.primer.android.internal.tokens.LightColorTokens
import io.primer.android.internal.tokens.RadiusTokens
import io.primer.android.internal.tokens.SizeTokens
import io.primer.android.internal.tokens.SpacingTokens
import io.primer.android.internal.tokens.TypographyTokens

data class PrimerTheme(
    val lightColorTokens: LightColorTokens = LightColorTokens(),
    val darkColorTokens: DarkColorTokens = DarkColorTokens(),
    val radiusTokens: RadiusTokens = RadiusTokens(),
    val sizeTokens: SizeTokens = SizeTokens(),
    val spacingTokens: SpacingTokens = SpacingTokens(),
    val typographyTokens: TypographyTokens = TypographyTokens(),
) {
    @Composable
    fun colorTokens(): LightColorTokens {
        return if (isSystemInDarkTheme()) {
            darkColorTokens
        } else {
            lightColorTokens
        }
    }
}

internal val LocalPrimerTheme = staticCompositionLocalOf {
    PrimerTheme()
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
    theme: PrimerTheme,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        createDarkColorScheme(theme.darkColorTokens)
    } else {
        createLightColorScheme(theme.lightColorTokens)
    }

    CompositionLocalProvider(
        LocalPrimerTheme provides theme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
