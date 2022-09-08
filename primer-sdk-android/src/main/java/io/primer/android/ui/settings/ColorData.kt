package io.primer.android.ui.settings

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import io.primer.android.utils.UiMode

sealed class ColorData {

    fun getColor(context: Context, isDarkMode: Boolean?): Int {
        val isDarkTheme = isDarkMode ?: UiMode.useDarkTheme(context)
        return when (this) {
            is ResourceColor -> {
                val id = if (isDarkTheme) this.dark else this.default
                ContextCompat.getColor(context, id)
            }
            is DynamicColor -> if (isDarkTheme) this.dark else this.default
        }
    }
}

class ResourceColor private constructor(
    @ColorRes val default: Int,
    @ColorRes val dark: Int,
) : ColorData() {

    companion object {

        fun valueOf(default: Int, dark: Int? = null): ResourceColor {
            return ResourceColor(default = default, dark = dark ?: default)
        }
    }
}

class DynamicColor private constructor(
    @ColorInt val default: Int,
    @ColorInt val dark: Int,
) : ColorData() {

    companion object {

        private val HEX_PATTERN = Regex("^#[0-9a-fA-F]{6,8}$")

        private fun hexToColorInt(hex: String): Int {
            return if (hex.matches(HEX_PATTERN)) {
                Color.parseColor(hex)
            } else {
                throw IllegalArgumentException("color input ($hex) is not a hex value")
            }
        }

        fun valueOf(default: String, dark: String? = null): DynamicColor {

            val mainColor = hexToColorInt(default)
            val darkColor = hexToColorInt(dark ?: default)

            return DynamicColor(default = mainColor, dark = darkColor)
        }
    }
}
