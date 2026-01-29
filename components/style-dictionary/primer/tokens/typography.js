import StyleDictionary from 'style-dictionary';

StyleDictionary.registerFormat({
  name: 'primer/android/compose/typography',
  format: ({ dictionary }) => {
    const typographyTokens = dictionary.allTokens
      .filter(token => token.path[1] === 'typography' && token.path.length === 5)
      .reduce((acc, token) => {
        const name = token.path[2] + token.path[3].charAt(0).toUpperCase() + token.path[3].slice(1);
        acc[name] = acc[name] || { font: `R.font.inter`, fontSize: undefined, fontWeight: undefined, letterSpacing: undefined, lineHeight: undefined };
        if (token.path[4] === 'size') acc[name].fontSize = `${token.value}`;
        if (token.path[4] === 'weight') acc[name].fontWeight = `${token.value}`;
        if (token.path[4] === 'letterSpacing') acc[name].letterSpacing = `${token.value}`;
        if (token.path[4] === 'lineHeight') acc[name].lineHeight = `${token.value}`;
        return acc;
      }, {});

    return `@file:Suppress("ALL")

package io.primer.android.internal.tokens

// Auto-generated file. Do not modify!

import androidx.annotation.FontRes
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.sp
import io.primer.android.components.R

data class TypographyTokens(
    ${Object.entries(typographyTokens)
      .map(([name, values]) =>
        `val ${name}: TypographyStyle = TypographyStyle(
        font = ${values.font},
        letterSpacing = ${values.letterSpacing}f,
        weight = ${values.fontWeight},
        size = ${values.fontSize},
        lineHeight = ${values.lineHeight}
    )`).join(",\n    ")}
)

data class TypographyStyle(
    @FontRes val font: Int,
    val letterSpacing: Float,
    val weight: Int,
    val size: Int,
    val lineHeight: Int
) {
    @OptIn(ExperimentalTextApi::class)
    fun toTextStyle(): TextStyle {
        return TextStyle(
            fontFamily = FontFamily(
                Font(
                    resId = font,
                    variationSettings = FontVariation.Settings(
                        FontVariation.weight(weight)
                    )
                )
            ),
            fontSize = size.sp,
            letterSpacing = letterSpacing.sp,
            lineHeight = lineHeight.sp
        )
    }
}

`;
  }
});

export default {
  destination: 'TypographyTokens.kt',
  format: 'primer/android/compose/typography',
};
