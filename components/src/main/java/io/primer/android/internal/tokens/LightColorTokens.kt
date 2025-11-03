@file:Suppress("ALL")

package io.primer.android.internal.tokens

// Auto-generated file. Do not modify!

import androidx.compose.ui.graphics.Color

/**
 * Light mode color tokens.
 *
 * Architecture:
 * - Base tokens (16): Direct color values that can be overridden in dark mode
 * - Semantic tokens (26): Computed properties that reference base tokens
 *
 * How dark mode works:
 * 1. DarkColorTokens extends LightColorTokens
 * 2. DarkColorTokens overrides only the 16 base token values
 * 3. The 26 semantic tokens (as getters) automatically resolve to overridden values
 *
 * Example flow:
 *   Light mode:
 *     primerColorBackground.get() → primerColorGray000 → Color(0xffffffff) ✓
 *
 *   Dark mode (DarkColorTokens overrides primerColorGray000):
 *     primerColorBackground.get() → primerColorGray000 → Color(0xff171619) ✓
 *
 * This is OOP polymorphism in action!
 */
open class LightColorTokens {
    // ========================================
    // BASE TOKENS (16)
    // ========================================
    // Direct color values that can be overridden in DarkColorTokens

    open val primerColorBorderTransparentDefault: Color = Color(0x00ffffff)
    open val primerColorGray100: Color = Color(0xfff5f5f5)
    open val primerColorGray200: Color = Color(0xffeeeeee)
    open val primerColorGray300: Color = Color(0xffe0e0e0)
    open val primerColorGray400: Color = Color(0xffbdbdbd)
    open val primerColorGray500: Color = Color(0xff9e9e9e)
    open val primerColorGray600: Color = Color(0xff757575)
    open val primerColorGray900: Color = Color(0xff212121)
    open val primerColorGray000: Color = Color(0xffffffff)
    open val primerColorGreen500: Color = Color(0xff3eb68f)
    open val primerColorBrand: Color = Color(0xff2f98ff)
    open val primerColorRed100: Color = Color(0xffffecec)
    open val primerColorRed500: Color = Color(0xffff7279)
    open val primerColorRed900: Color = Color(0xffb4324b)
    open val primerColorBlue500: Color = Color(0xff399dff)
    open val primerColorBlue900: Color = Color(0xff2270f4)

    // ========================================
    // SEMANTIC TOKENS (26)
    // ========================================
    // Computed properties that reference base tokens
    // These automatically resolve to overridden values in dark mode

    // Resolves to Color(0xffffffff) in light mode
    open val primerColorBackground: Color
        get() = primerColorGray000

    // Resolves to Color(0xff212121) in light mode
    open val primerColorTextPrimary: Color
        get() = primerColorGray900

    // Resolves to Color(0xff9e9e9e) in light mode
    open val primerColorTextPlaceholder: Color
        get() = primerColorGray500

    // Resolves to Color(0xffbdbdbd) in light mode
    open val primerColorTextDisabled: Color
        get() = primerColorGray400

    // Resolves to Color(0xffb4324b) in light mode
    open val primerColorTextNegative: Color
        get() = primerColorRed900

    // Resolves to Color(0xff2270f4) in light mode
    open val primerColorTextLink: Color
        get() = primerColorBlue900

    // Resolves to Color(0xff757575) in light mode
    open val primerColorTextSecondary: Color
        get() = primerColorGray600

    // Resolves to Color(0xffe0e0e0) in light mode
    open val primerColorBorderOutlinedDefault: Color
        get() = primerColorGray300

    // Resolves to Color(0xffbdbdbd) in light mode
    open val primerColorBorderOutlinedHover: Color
        get() = primerColorGray400

    // Resolves to Color(0xff9e9e9e) in light mode
    open val primerColorBorderOutlinedActive: Color
        get() = primerColorGray500

    // Resolves to Color(0xff2f98ff) in light mode
    open val primerColorBorderOutlinedFocus: Color
        get() = primerColorFocus

    // Resolves to Color(0xffeeeeee) in light mode
    open val primerColorBorderOutlinedDisabled: Color
        get() = primerColorGray200

    // Resolves to Color(0xffeeeeee) in light mode
    open val primerColorBorderOutlinedLoading: Color
        get() = primerColorGray200

    // Resolves to Color(0xff2f98ff) in light mode
    open val primerColorBorderOutlinedSelected: Color
        get() = primerColorBrand

    // Resolves to Color(0xffff7279) in light mode
    open val primerColorBorderOutlinedError: Color
        get() = primerColorRed500

    // Resolves to Color(0x00ffffff) in light mode
    open val primerColorBorderTransparentHover: Color
        get() = primerColorBorderTransparentDefault

    // Resolves to Color(0x00ffffff) in light mode
    open val primerColorBorderTransparentActive: Color
        get() = primerColorBorderTransparentDefault

    // Resolves to Color(0xff2f98ff) in light mode
    open val primerColorBorderTransparentFocus: Color
        get() = primerColorFocus

    // Resolves to Color(0x00ffffff) in light mode
    open val primerColorBorderTransparentDisabled: Color
        get() = primerColorBorderTransparentDefault

    // Resolves to Color(0x00ffffff) in light mode
    open val primerColorBorderTransparentSelected: Color
        get() = primerColorBorderTransparentDefault

    // Resolves to Color(0xff212121) in light mode
    open val primerColorIconPrimary: Color
        get() = primerColorGray900

    // Resolves to Color(0xffbdbdbd) in light mode
    open val primerColorIconDisabled: Color
        get() = primerColorGray400

    // Resolves to Color(0xffff7279) in light mode
    open val primerColorIconNegative: Color
        get() = primerColorRed500

    // Resolves to Color(0xff3eb68f) in light mode
    open val primerColorIconPositive: Color
        get() = primerColorGreen500

    // Resolves to Color(0xff2f98ff) in light mode
    open val primerColorFocus: Color
        get() = primerColorBrand

    // Resolves to Color(0xff2f98ff) in light mode
    open val primerColorLoader: Color
        get() = primerColorBrand
}