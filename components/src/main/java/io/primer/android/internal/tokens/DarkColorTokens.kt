@file:Suppress("ALL")

package io.primer.android.internal.tokens

// Auto-generated file. Do not modify

import androidx.compose.ui.graphics.Color

/**
 * Dark mode color tokens.
 *
 * Architecture:
 * - Extends LightColorTokens to inherit all semantic tokens
 * - Overrides only 15 base color values
 * - Semantic tokens (27) are automatically inherited as computed properties
 *
 * How it works:
 * 1. This class extends LightColorTokens
 * 2. It overrides only the base token values (direct colors)
 * 3. Semantic tokens are inherited as getters from LightColorTokens
 * 4. Those getters automatically resolve to the overridden base values
 *
 * Example runtime resolution:
 *   primerColorBackground.get()
 *     → calls inherited getter
 *     → getter returns primerColorGray000
 *     → resolves to OVERRIDDEN value: Color(0xff171619) ✓
 *
 * vs Light mode:
 *   primerColorBackground.get()
 *     → calls same getter
 *     → getter returns primerColorGray000
 *     → resolves to base value: Color(0xffffffff) ✓
 *
 * Same getter, different values! That's polymorphism.
 */
class DarkColorTokens : LightColorTokens() {
    // ========================================
    // BASE TOKEN OVERRIDES (15)
    // ========================================
    // Override base token values for dark mode
    // Semantic tokens automatically inherit these new values

    override val primerColorGray100: Color = Color(0xff292929)
    override val primerColorGray200: Color = Color(0xff424242)
    override val primerColorGray300: Color = Color(0xff575757)
    override val primerColorGray400: Color = Color(0xff858585)
    override val primerColorGray500: Color = Color(0xff767577)
    override val primerColorGray600: Color = Color(0xffc7c7c7)
    override val primerColorGray900: Color = Color(0xffefefef)
    override val primerColorGray000: Color = Color(0xff171619)
    override val primerColorGreen500: Color = Color(0xff27b17d)
    override val primerColorBrand: Color = Color(0xff2f98ff)
    override val primerColorRed100: Color = Color(0xff321c20)
    override val primerColorRed500: Color = Color(0xffe46d70)
    override val primerColorRed900: Color = Color(0xfff6bfbf)
    override val primerColorBlue500: Color = Color(0xff3f93e4)
    override val primerColorBlue900: Color = Color(0xff4aaeff)
}