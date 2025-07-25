import StyleDictionary from 'style-dictionary';
import { generateKotlinDataClass, processDpTokens } from './utils.js';

StyleDictionary.registerFormat({
  name: 'primer/android/compose/size',
  format: ({ dictionary }) =>
    generateKotlinDataClass(
      'io.primer.android.internal.tokens',
      'import androidx.compose.ui.unit.Dp\nimport androidx.compose.ui.unit.dp',
      'SizeTokens',
      processDpTokens(dictionary, token => token.path[1] === 'size', 'primer.size.base')
    )
});

export default {
  destination: 'SizeTokens.kt',
  format: 'primer/android/compose/size',
};
