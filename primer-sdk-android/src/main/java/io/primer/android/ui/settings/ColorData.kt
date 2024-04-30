package io.primer.android.ui.settings

import android.content.Context
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import io.primer.android.utils.UiMode

sealed class ColorData : Parcelable {

    abstract fun getColor(context: Context, isDarkMode: Boolean?): Int

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        when (this) {
            is ResourceColor -> {
                parcel.writeInt(1)
                parcel.writeInt(default)
                parcel.writeInt(dark)
            }

            is DynamicColor -> {
                parcel.writeInt(2)
                parcel.writeInt(default)
                parcel.writeInt(dark)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ColorData> = object : Parcelable.Creator<ColorData> {

            override fun createFromParcel(parcel: Parcel): ColorData {
                return when (parcel.readInt()) {
                    1 -> ResourceColor.valueOf(parcel.readInt(), parcel.readInt())
                    2 -> DynamicColor(parcel.readInt(), parcel.readInt())
                    else -> throw IllegalArgumentException("Unknown type")
                }
            }

            override fun newArray(size: Int): Array<ColorData?> {
                return arrayOfNulls(size)
            }
        }
    }
}

class ResourceColor private constructor(
    @ColorRes val default: Int,
    @ColorRes val dark: Int
) : ColorData() {

    override fun getColor(context: Context, isDarkMode: Boolean?): Int {
        val isDarkTheme = isDarkMode ?: UiMode.useDarkTheme(context)
        return ContextCompat.getColor(context, if (isDarkTheme) dark else default)
    }

    companion object {
        fun valueOf(default: Int, dark: Int? = null): ResourceColor {
            return ResourceColor(default = default, dark = dark ?: default)
        }
    }
}

class DynamicColor(
    @ColorInt val default: Int,
    @ColorInt val dark: Int
) : ColorData() {

    override fun getColor(context: Context, isDarkMode: Boolean?): Int {
        val isDarkTheme = isDarkMode ?: UiMode.useDarkTheme(context)
        return if (isDarkTheme) dark else default
    }

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
