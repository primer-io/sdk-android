import StyleDictionary from 'style-dictionary';
import darkColorsConfig from './tokens/dark-colors.js';

StyleDictionary.registerTransformGroup({
  name: 'primer-android-compose-dark',
  transforms: ['color/composeColor', 'name/camel'],
});

export default {
  source: ['primer/dark.json'],
  platforms: {
    android: {
      transformGroup: 'primer-android-compose-dark',
      buildPath: '../src/main/java/io/primer/components/styleDictionary/',
      files: [darkColorsConfig],
    },
  },
};
