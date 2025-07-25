import StyleDictionary from 'style-dictionary';
import { generateKotlinDataClass, processDpTokens } from './utils.js';

StyleDictionary.registerFormat({
  name: 'primer/android/compose/spacing',
  format: ({ dictionary }) =>
    generateKotlinDataClass(
      'io.primer.android.internal.tokens',
      'import androidx.compose.ui.unit.Dp\nimport androidx.compose.ui.unit.dp',
      'SpacingTokens',
      processDpTokens(dictionary, token => token.path[1] === 'space', 'primer.space.base')
    )
});

export default {
  destination: 'SpacingTokens.kt',
  format: 'primer/android/compose/spacing',
};
