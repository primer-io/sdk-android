import StyleDictionary from 'style-dictionary';
import lightColorsConfig from './tokens/light-colors.js';
import radiusConfig from './tokens/radius.js';
import spacingConfig from './tokens/spacing.js';
import sizeConfig from './tokens/size.js';
import typographyConfig from './tokens/typography.js';

StyleDictionary.registerTransformGroup({
  name: 'primer-android-compose',
  transforms: ['color/composeColor', 'name/camel'],
});

export default {
  source: ['primer/base.json'],
  platforms: {
    android: {
      transformGroup: 'primer-android-compose',
      buildPath: '../src/main/java/io/primer/composable/internal/tokens/',
      files: [
        lightColorsConfig,
        radiusConfig,
        typographyConfig,
        spacingConfig,
        sizeConfig
      ],
    },
  },
};
