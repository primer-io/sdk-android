package io.primer.sample.demos

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.primer.android.PrimerTheme
import io.primer.android.internal.tokens.LightColorTokens
import io.primer.android.internal.tokens.RadiusTokens
import io.primer.android.internal.tokens.SizeTokens
import io.primer.android.internal.tokens.TypographyStyle
import io.primer.android.internal.tokens.TypographyTokens

object RedThemeDemo : CheckoutDemo(
    title = "Red Brand Theme",
    description = "Red brand color with custom accents",
    customizationLevel = 1,
    theme = PrimerTheme(lightColorTokens = object : LightColorTokens() {
        override val primerColorBrand = Color(0xFFE53E3E)
        override val primerColorBorderOutlinedFocus = Color(0xFFE53E3E)
        override val primerColorBorderOutlinedDefault = Color(0xFFE53E3E)
    }),
    render = {}
)

object GreenThemeDemo : CheckoutDemo(
    title = "Green Nature Theme",
    description = "Green brand with nature-inspired colors",
    customizationLevel = 1,
    theme = PrimerTheme(lightColorTokens = object : LightColorTokens() {
        override val primerColorBrand = Color(0xFF38A169)
        override val primerColorBorderOutlinedFocus = Color(0xFF38A169)
        override val primerColorBorderOutlinedDefault = Color(0xFF38A169)
    }),
    render = {}
)

object PurpleThemeDemo : CheckoutDemo(
    title = "Purple Creative Theme",
    description = "Purple brand with creative vibes",
    customizationLevel = 1,
    theme = PrimerTheme(lightColorTokens = object : LightColorTokens() {
        override val primerColorBrand = Color(0xFF805AD5)
        override val primerColorBorderOutlinedFocus = Color(0xFF805AD5)
        override val primerColorBorderOutlinedDefault = Color(0xFF805AD5)
    }),
    render = {}
)

object NoRadiusThemeDemo : CheckoutDemo(
    title = "No Radius Theme",
    description = "Sharp, rectangular design with zero border radius",
    customizationLevel = 1,
    theme = PrimerTheme(
        radiusTokens = RadiusTokens(
            medium = 0.dp,
            small = 0.dp,
            large = 0.dp,
            xsmall = 0.dp,
            base = 0.dp
        )
    ),
    render = {}
)

object SmallSizesThemeDemo : CheckoutDemo(
    title = "Small Sizes Theme",
    description = "Compact design with smaller component sizes",
    customizationLevel = 1,
    theme = PrimerTheme(
        sizeTokens = SizeTokens(
            small = 12.dp,
            medium = 16.dp,
            large = 18.dp,
            xlarge = 22.dp,
            xxlarge = 28.dp,
            xxxlarge = 34.dp
        )
    ),
    render = {}
)

object LargeSizesThemeDemo : CheckoutDemo(
    title = "Large Sizes Theme",
    description = "Bold design with larger component sizes",
    customizationLevel = 1,
    theme = PrimerTheme(
        sizeTokens = SizeTokens(
            small = 24.dp,
            medium = 32.dp,
            large = 40.dp,
            xlarge = 48.dp,
            xxlarge = 64.dp,
            xxxlarge = 80.dp
        )
    ),
    render = {}
)

object RegularTypographyThemeDemo : CheckoutDemo(
    title = "All Regular Typography",
    description = "Clean, minimal typography with regular font weights",
    customizationLevel = 1,
    theme = PrimerTheme(
        typographyTokens = TypographyTokens(
            titleXlarge = TypographyStyle(
                font = "Inter",
                weight = 400,
                size = 24,
                lineHeight = 32,
                letterSpacing = -0.6f
            ),
            titleLarge = TypographyStyle(
                font = "Inter",
                weight = 400,
                size = 16,
                lineHeight = 20,
                letterSpacing = -0.2f
            )
        )
    ),
    render = {}
)

object BoldTypographyThemeDemo : CheckoutDemo(
    title = "All Bold Typography",
    description = "Strong, impactful typography with bold font weights",
    customizationLevel = 1,
    theme = PrimerTheme(
        typographyTokens = TypographyTokens(
            titleXlarge = TypographyStyle(
                font = "Inter",
                weight = 700,
                size = 24,
                lineHeight = 32,
                letterSpacing = -0.6f
            ),
            titleLarge = TypographyStyle(
                font = "Inter",
                weight = 700,
                size = 16,
                lineHeight = 20,
                letterSpacing = -0.2f
            ),
            bodyLarge = TypographyStyle(
                font = "Inter",
                weight = 700,
                size = 16,
                lineHeight = 20,
                letterSpacing = -0.2f
            ),
            bodyMedium = TypographyStyle(font = "Inter", weight = 700, size = 14, lineHeight = 20, letterSpacing = 0f),
            bodySmall = TypographyStyle(font = "Inter", weight = 700, size = 12, lineHeight = 16, letterSpacing = 0f)
        )
    ),
    render = {}
)

object LargeTypographyThemeDemo : CheckoutDemo(
    title = "Large Typography (32sp)",
    description = "Accessibility-focused design with large text sizes",
    customizationLevel = 1,
    theme = PrimerTheme(
        typographyTokens = TypographyTokens(
            titleXlarge = TypographyStyle(
                font = "Inter",
                weight = 550,
                size = 32,
                lineHeight = 40,
                letterSpacing = -0.6f
            ),
            titleLarge = TypographyStyle(
                font = "Inter",
                weight = 550,
                size = 32,
                lineHeight = 38,
                letterSpacing = -0.2f
            ),
            bodyLarge = TypographyStyle(
                font = "Inter",
                weight = 400,
                size = 32,
                lineHeight = 38,
                letterSpacing = -0.2f
            ),
            bodyMedium = TypographyStyle(font = "Inter", weight = 400, size = 32, lineHeight = 40, letterSpacing = 0f),
            bodySmall = TypographyStyle(font = "Inter", weight = 400, size = 32, lineHeight = 38, letterSpacing = 0f)
        )
    ),
    render = {}
)
