import StyleDictionary from 'style-dictionary';
import { generateKotlinDataClass, processDpTokens } from './utils.js';

StyleDictionary.registerFormat({
  name: 'primer/android/compose/radius',
  format: ({ dictionary }) =>
    generateKotlinDataClass(
      'io.primer.composable.internal.tokens',
      'import androidx.compose.ui.unit.Dp\nimport androidx.compose.ui.unit.dp',
      'RadiusTokens',
      processDpTokens(dictionary, token => token.path[1] === 'radius', 'primer.radius.base')
    )
});

export default {
  destination: 'RadiusTokens.kt',
  format: 'primer/android/compose/radius',
};
