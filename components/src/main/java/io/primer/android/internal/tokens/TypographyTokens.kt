@file:Suppress("ALL")

package io.primer.android.internal.tokens

// Auto-generated file. Do not modify!

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.primer.android.components.R

data class TypographyTokens(
    val titleXlarge: TypographyStyle = TypographyStyle(
        font = "Inter",
        letterSpacing = -0.6f,
        weight = 550,
        size = 24,
        lineHeight = 32
    ),
    val titleLarge: TypographyStyle = TypographyStyle(
        font = "Inter",
        letterSpacing = -0.2f,
        weight = 550,
        size = 16,
        lineHeight = 20
    ),
    val bodyLarge: TypographyStyle = TypographyStyle(
        font = "Inter",
        letterSpacing = -0.2f,
        weight = 400,
        size = 16,
        lineHeight = 20
    ),
    val bodyMedium: TypographyStyle = TypographyStyle(
        font = "Inter",
        letterSpacing = 0f,
        weight = 400,
        size = 14,
        lineHeight = 20
    ),
    val bodySmall: TypographyStyle = TypographyStyle(
        font = "Inter",
        letterSpacing = 0f,
        weight = 400,
        size = 12,
        lineHeight = 16
    )
)

data class TypographyStyle(
    val font: String,
    val letterSpacing: Float,
    val weight: Int,
    val size: Int,
    val lineHeight: Int
) {
    fun toTextStyle(): TextStyle {
        return TextStyle(
            fontFamily = getFontFamily(font),
            fontSize = size.sp,
            fontWeight = FontWeight(weight),
            letterSpacing = letterSpacing.sp,
            lineHeight = lineHeight.sp
        )
    }

    @OptIn(ExperimentalTextApi::class)
    private fun getFontFamily(fontName: String): FontFamily {
        return when (fontName.lowercase()) {
            "inter" -> FontFamily(
                Font(
                    R.font.inter, variationSettings = FontVariation.Settings(
                        FontVariation.weight(550),
                    )
                )
            ) // Add more fonts here if needed
            else -> FontFamily.Default
        }
    }
}

