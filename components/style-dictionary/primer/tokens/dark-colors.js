/**
 * Dark Mode Color Token Generator
 *
 * Generates DarkColorTokens.kt that extends LightColorTokens.
 *
 * Key Concept:
 * - Only overrides BASE TOKEN values (direct color values)
 * - Does NOT touch semantic tokens (they're inherited as getters)
 *
 * Why this works:
 * 1. LightColorTokens has semantic tokens as computed properties (getters)
 * 2. Those getters reference base tokens (e.g., primerColorGray000)
 * 3. DarkColorTokens overrides the base token values
 * 4. The semantic token getters automatically resolve to the new values!
 *
 * Example:
 *   // LightColorTokens
 *   open val primerColorGray000: Color = Color(0xffffffff)  // white
 *   open val primerColorBackground: Color
 *       get() = primerColorGray000                          // -> white
 *
 *   // DarkColorTokens extends LightColorTokens
 *   override val primerColorGray000: Color = Color(0xff171619)  // dark
 *   // primerColorBackground getter inherited -> now resolves to dark!
 *
 * This is the magic of OOP polymorphism!
 */

import StyleDictionary from 'style-dictionary';

StyleDictionary.registerFormat({
  name: 'primer/android/compose/colors-dark',
  format: ({ dictionary }) => {
    // Get all color tokens
    const colorTokens = dictionary.allTokens
      .filter(token => token.path[0] === 'primer' && token.path[1] === 'color');

    // ========================================
    // STEP 1: Filter to only base tokens
    // ========================================
    // Only base tokens exist in dark.json
    // Semantic tokens are inherited from LightColorTokens
    const baseTokens = colorTokens
      .filter(token => {
        const originalValue = token.original.value;

        // Check if this is a reference (semantic token)
        const isReference =
          typeof originalValue === 'string' &&
          originalValue.startsWith('{') &&
          originalValue.endsWith('}');

        // We only want NON-references (base tokens)
        return !isReference;
      })
      .map(token => ({
        name: token.name,
        value: token.value,
      }));

    // ========================================
    // STEP 2: Generate Kotlin override code
    // ========================================
    const overrideProperties = baseTokens
      .map(token => `    override val ${token.name}: Color = ${token.value}`)
      .join('\n');

    // ========================================
    // STEP 3: Generate complete Kotlin file
    // ========================================
    return `@file:Suppress("ALL")

package io.primer.android.internal.tokens

// Auto-generated file. Do not modify

import androidx.compose.ui.graphics.Color

/**
 * Dark mode color tokens.
 *
 * Architecture:
 * - Extends LightColorTokens to inherit all semantic tokens
 * - Overrides only ${baseTokens.length} base color values
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
    // BASE TOKEN OVERRIDES (${baseTokens.length})
    // ========================================
    // Override base token values for dark mode
    // Semantic tokens automatically inherit these new values

${overrideProperties}
}`;
  }
});

export default {
  destination: 'DarkColorTokens.kt',
  format: 'primer/android/compose/colors-dark',
};
