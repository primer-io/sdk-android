package io.primer.android.data.settings

import android.content.Context
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.utils.UiMode
import org.json.JSONObject

enum class ColorDataType(val value: Int) {
    RESOURCE_COLOR(1),
    DYNAMIC_COLOR(2),
    ;

    companion object {
        fun fromValue(value: Int): ColorDataType {
            return ColorDataType.entries.first { it.value == value }
        }
    }
}

sealed class ColorData(private val dataType: ColorDataType) : Parcelable, JSONObjectSerializable {
    abstract fun getColor(
        context: Context,
        isDarkMode: Boolean?,
    ): Int

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        when (this) {
            is ResourceColor -> {
                parcel.writeInt(dataType.value)
                parcel.writeInt(default)
                parcel.writeInt(dark)
            }

            is DynamicColor -> {
                parcel.writeInt(dataType.value)
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
        val CREATOR: Parcelable.Creator<ColorData> =
            object : Parcelable.Creator<ColorData> {
                override fun createFromParcel(parcel: Parcel): ColorData {
                    return when (ColorDataType.fromValue(parcel.readInt())) {
                        ColorDataType.RESOURCE_COLOR -> ResourceColor.valueOf(parcel.readInt(), parcel.readInt())
                        ColorDataType.DYNAMIC_COLOR -> DynamicColor.valueOf(parcel.readInt(), parcel.readInt())
                    }
                }

                override fun newArray(size: Int): Array<ColorData?> {
                    return arrayOfNulls(size)
                }
            }

        private const val DATA_TYPE_FIELD = "dataType"
        private const val DEFAULT_COLOR_FIELD = "defaultColor"
        private const val DARK_COLOR_FIELD = "darkColor"

        @JvmField
        val serializer =
            JSONObjectSerializer<ColorData> { t ->
                JSONObject().apply {
                    put(DATA_TYPE_FIELD, t.dataType.name)
                    when (t) {
                        is ResourceColor -> {
                            put(DEFAULT_COLOR_FIELD, t.default)
                            put(DARK_COLOR_FIELD, t.dark)
                        }

                        is DynamicColor -> {
                            put(DEFAULT_COLOR_FIELD, t.default)
                            put(DARK_COLOR_FIELD, t.dark)
                        }
                    }
                }
            }
    }
}

class ResourceColor private constructor(
    @ColorRes val default: Int,
    @ColorRes val dark: Int,
) : ColorData(ColorDataType.RESOURCE_COLOR) {
    override fun getColor(
        context: Context,
        isDarkMode: Boolean?,
    ): Int {
        val isDarkTheme = isDarkMode ?: UiMode.useDarkTheme(context)
        return ContextCompat.getColor(context, if (isDarkTheme) dark else default)
    }

    companion object {
        fun valueOf(
            default: Int,
            dark: Int? = null,
        ): ResourceColor {
            return ResourceColor(default = default, dark = dark ?: default)
        }
    }
}

class DynamicColor private constructor(
    @ColorInt val default: Int,
    @ColorInt val dark: Int,
) : ColorData(ColorDataType.DYNAMIC_COLOR) {
    override fun getColor(
        context: Context,
        isDarkMode: Boolean?,
    ): Int {
        val isDarkTheme = isDarkMode ?: UiMode.useDarkTheme(context)
        return if (isDarkTheme) dark else default
    }

    companion object {
        private val HEX_PATTERN = Regex("^#[0-9a-fA-F]{6,8}$")

        private fun hexToColorInt(hex: String): Int {
            require(hex.matches(HEX_PATTERN)) { "color input ($hex) is not a hex value" }
            return Color.parseColor(hex)
        }

        fun valueOf(
            default: String,
            dark: String? = null,
        ): DynamicColor {
            val mainColor = hexToColorInt(default)
            val darkColor = hexToColorInt(dark ?: default)
            return DynamicColor(default = mainColor, dark = darkColor)
        }

        fun valueOf(
            mainColor: Int,
            darkColor: Int,
        ) = DynamicColor(default = mainColor, dark = darkColor)
    }
}
