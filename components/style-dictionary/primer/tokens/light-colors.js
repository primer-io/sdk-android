/**
 * Light Mode Color Token Generator
 *
 * Generates LightColorTokens.kt with proper base/semantic token separation.
 *
 * Key Concepts:
 * - BASE TOKENS: Direct color values (e.g., gray.000 = #ffffff)
 * - SEMANTIC TOKENS: References to base tokens (e.g., background = {gray.000})
 *
 * Why this matters:
 * - Base tokens can be overridden in DarkColorTokens
 * - Semantic tokens use computed properties (getters) to reference base tokens
 * - This makes dark mode work automatically through OOP inheritance
 *
 * Example:
 *   Light: primerColorBackground -> primerColorGray000 -> #ffffff (white)
 *   Dark:  primerColorBackground -> primerColorGray000 -> #171619 (dark)
 *          ↑ same getter              ↑ overridden value
 */

import StyleDictionary from 'style-dictionary';

StyleDictionary.registerFormat({
  name: 'primer/android/compose/colors',
  format: ({ dictionary }) => {
    // Get all color tokens
    const colorTokens = dictionary.allTokens
      .filter(token => token.path[0] === 'primer' && token.path[1] === 'color');

    // ========================================
    // STEP 1: Classify tokens
    // ========================================
    const baseTokens = [];
    const semanticTokens = [];

    colorTokens.forEach(token => {
      const originalValue = token.original.value;

      // Check if this token references another token (semantic)
      // Example: "{primer.color.gray.000}" is a reference
      const isReference =
        typeof originalValue === 'string' &&
        originalValue.startsWith('{') &&
        originalValue.endsWith('}');

      if (isReference) {
        // SEMANTIC TOKEN: Convert reference to property name
        // "{primer.color.gray.000}" -> "primerColorGray000"
        const referencePath = originalValue
          .replace(/[{}]/g, '')                    // Remove braces
          .split('.')                              // Split by dot
          .map((part, index) => {
            if (index === 0) return part;          // Keep first lowercase
            return part.charAt(0).toUpperCase() + part.slice(1); // Capitalize rest
          })
          .join('');

        semanticTokens.push({
          name: token.name,
          reference: referencePath,
          resolvedValue: token.value,
        });
      } else {
        // BASE TOKEN: Direct color value
        baseTokens.push({
          name: token.name,
          value: token.value,
        });
      }
    });

    // ========================================
    // STEP 2: Generate Kotlin code
    // ========================================

    // Base tokens: Regular properties with color values
    const baseProperties = baseTokens
      .map(token => `    open val ${token.name}: Color = ${token.value}`)
      .join('\n');

    // Semantic tokens: Computed properties (getters) that reference base tokens
    const semanticProperties = semanticTokens
      .map(token =>
        `    // Resolves to ${token.resolvedValue} in light mode\n` +
        `    open val ${token.name}: Color\n` +
        `        get() = ${token.reference}`
      )
      .join('\n\n');

    // ========================================
    // STEP 3: Generate complete Kotlin file
    // ========================================
    return `@file:Suppress("ALL")

package io.primer.android.internal.tokens

// Auto-generated file. Do not modify!

import androidx.compose.ui.graphics.Color

/**
 * Light mode color tokens.
 *
 * Architecture:
 * - Base tokens (${baseTokens.length}): Direct color values that can be overridden in dark mode
 * - Semantic tokens (${semanticTokens.length}): Computed properties that reference base tokens
 *
 * How dark mode works:
 * 1. DarkColorTokens extends LightColorTokens
 * 2. DarkColorTokens overrides only the ${baseTokens.length} base token values
 * 3. The ${semanticTokens.length} semantic tokens (as getters) automatically resolve to overridden values
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
    // BASE TOKENS (${baseTokens.length})
    // ========================================
    // Direct color values that can be overridden in DarkColorTokens

${baseProperties}

    // ========================================
    // SEMANTIC TOKENS (${semanticTokens.length})
    // ========================================
    // Computed properties that reference base tokens
    // These automatically resolve to overridden values in dark mode

${semanticProperties}
}`;
  }
});

export default {
  destination: 'LightColorTokens.kt',
  format: 'primer/android/compose/colors',
};
