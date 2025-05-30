import StyleDictionary from 'style-dictionary';

StyleDictionary.registerFormat({
  name: 'primer/android/compose/colors',
  format: ({ dictionary }) => {
    const tokens = dictionary.allTokens
      .filter(token => token.path[0] === 'primer' && token.path[1] === 'color')
      .map(token => `override val ${token.name}: Color = ${token.value}`)
      .join('\n    ');

    return `@file:Suppress("ALL")

package io.primer.composable.internal.tokens

// Auto-generated file. Do not modify

import androidx.compose.ui.graphics.Color

class DarkColorTokens : LightColorTokens() {
    ${tokens}
}`;
  }
});

export default {
  destination: 'DarkColorTokens.kt',
  format: 'primer/android/compose/colors',
};
