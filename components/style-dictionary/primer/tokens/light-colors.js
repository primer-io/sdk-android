import StyleDictionary from 'style-dictionary';

StyleDictionary.registerFormat({
  name: 'primer/android/compose/colors',
  format: ({ dictionary }) => {
    const tokens = dictionary.allTokens
      .filter(token => token.path[0] === 'primer' && token.path[1] === 'color')
      .map(token => `open val ${token.name}: Color = ${token.value}`)
      .join(',\n    ');

    return `@file:Suppress("ALL")

package io.primer.android.internal.tokens

// Auto-generated file. Do not modify!

import androidx.compose.ui.graphics.Color

open class LightColorTokens(
    ${tokens}
)`;
  }
});

export default {
  destination: 'LightColorTokens.kt',
  format: 'primer/android/compose/colors',
};
