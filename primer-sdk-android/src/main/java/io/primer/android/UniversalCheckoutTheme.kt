package io.primer.android

import android.graphics.Color
import androidx.annotation.ColorInt
import kotlinx.serialization.Serializable

// FIXME all these should be declared as resources
private const val BUTTON_CORNER_RADIUS_DEFAULT = 12.0f
private const val INPUT_CORNER_RADIUS_DEFAULT = 12.0f
private const val BACKGROUND_COLOR_DEFAULT = "#FFFFFFFF"
private const val BUTTON_PRIMARY_COLOR_DEFAULT = "#FF2C98F0"
private const val BUTTON_PRIMARY_COLOR_DISABLED_DEFAULT = "#8FBEC2C4"
private const val BUTTON_DEFAULT_COLOR_DEFAULT = "#FFFFFFFF"
private const val BUTTON_DEFAULT_BORDER_COLOR_DEFAULT = "#FFBEC2C4"
private const val BUTTON_DEFAULT_COLOR_DISABLED_DEFAULT = "#8FBEC2C4"
private const val TEXT_DEFAULT_COLOR_DEFAULT = "#FF000000"
private const val TEXT_DANGER_COLOR_DEFAULT = "#FFEB001B"
private const val TEXT_MUTED_COLOR_DEFAULT = "#FF808080"
private const val PRIMARY_COLOR_DEFAULT = "#FF2C98F0"
private const val INPUT_BACKGROUND_COLOR_DEFAULT = "#FFFFFFFF"

@Serializable
// FIXME consider resource references instead of values
data class UniversalCheckoutTheme constructor(
    val buttonCornerRadius: Float,
    val inputCornerRadius: Float,

    @ColorInt val backgroundColor: Int,

    @ColorInt val buttonPrimaryColor: Int,
    @ColorInt val buttonPrimaryColorDisabled: Int,
    @ColorInt val buttonDefaultColor: Int,
    @ColorInt val buttonDefaultColorDisabled: Int,
    @ColorInt val buttonDefaultBorderColor: Int,

    @ColorInt val textDefaultColor: Int,
    @ColorInt val textDangerColor: Int,
    @ColorInt val textMutedColor: Int,

    @ColorInt val primaryColor: Int,
    @ColorInt val inputBackgroundColor: Int,

    val windowMode: WindowMode,
) {

    enum class WindowMode {
        BOTTOM_SHEET,
        FULL_HEIGHT,
    }

    companion object {

        private val HEX_PATTERN = Regex("^#[0-9a-fA-F]{6,8}$")

        // FIXME unnecessary indirection
        fun getDefault(): UniversalCheckoutTheme {
            return create()
        }

        // FIXME drop all these static methods
        // FIXME theming should rely on android's color system, not hex values
        fun create(
            buttonCornerRadius: Float? = null,
            inputCornerRadius: Float? = null,
            backgroundColor: String? = null,
            buttonPrimaryColor: String? = null,
            buttonPrimaryColorDisabled: String? = null,
            buttonDefaultColor: String? = null,
            buttonDefaultColorDisabled: String? = null,
            buttonDefaultBorderColor: String? = null,
            textDefaultColor: String? = null,
            textDangerColor: String? = null,
            textMutedColor: String? = null,
            primaryColor: String? = null,
            inputBackgroundColor: String? = null,
            windowMode: WindowMode = WindowMode.BOTTOM_SHEET,
        ): UniversalCheckoutTheme {
            return UniversalCheckoutTheme(
                buttonCornerRadius = buttonCornerRadius ?: BUTTON_CORNER_RADIUS_DEFAULT,
                inputCornerRadius = inputCornerRadius ?: INPUT_CORNER_RADIUS_DEFAULT,
                backgroundColor = hexToColorInt(backgroundColor, BACKGROUND_COLOR_DEFAULT),
                buttonPrimaryColor = hexToColorInt(
                    buttonPrimaryColor,
                    BUTTON_PRIMARY_COLOR_DEFAULT
                ),
                buttonPrimaryColorDisabled = hexToColorInt(
                    buttonPrimaryColorDisabled,
                    BUTTON_PRIMARY_COLOR_DISABLED_DEFAULT
                ),
                buttonDefaultColor = hexToColorInt(
                    buttonDefaultColor,
                    BUTTON_DEFAULT_COLOR_DEFAULT
                ),
                buttonDefaultColorDisabled = hexToColorInt(
                    buttonDefaultColorDisabled,
                    BUTTON_DEFAULT_COLOR_DISABLED_DEFAULT
                ),
                buttonDefaultBorderColor = hexToColorInt(
                    buttonDefaultBorderColor,
                    BUTTON_DEFAULT_BORDER_COLOR_DEFAULT
                ),
                textDefaultColor = hexToColorInt(textDefaultColor, TEXT_DEFAULT_COLOR_DEFAULT),
                textDangerColor = hexToColorInt(textDangerColor, TEXT_DANGER_COLOR_DEFAULT),
                textMutedColor = hexToColorInt(textMutedColor, TEXT_MUTED_COLOR_DEFAULT),
                primaryColor = hexToColorInt(primaryColor, PRIMARY_COLOR_DEFAULT),
                inputBackgroundColor = hexToColorInt(
                    inputBackgroundColor,
                    INPUT_BACKGROUND_COLOR_DEFAULT
                ),
                windowMode = windowMode,
            )
        }

        // FIXME this should be moved elsewhere. this class should not hold this responsibility
        private fun hexToColorInt(hex: String?, defaultValue: String): Int {
            return Color.parseColor(
                if (hex?.matches(HEX_PATTERN) == true) hex else defaultValue
            )
        }
    }
}
