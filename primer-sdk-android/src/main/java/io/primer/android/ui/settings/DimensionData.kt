package io.primer.android.ui.settings

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DimenRes

sealed class DimensionData : Parcelable {

    fun getDimension(context: Context): Float {
        return when (this) {
            is ResourceDimension -> context.resources.getDimension(this.default)
            is DynamicDimension -> this.default
        }
    }

    fun getPixels(context: Context): Int {
        return when (this) {
            is ResourceDimension -> context.resources.getDimensionPixelSize(this.default)
            is DynamicDimension -> this.default.toInt()
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        when (this) {
            is ResourceDimension -> {
                parcel.writeInt(1)
                parcel.writeInt(default)
            }
            is DynamicDimension -> {
                parcel.writeInt(2)
                parcel.writeFloat(default)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DimensionData> = object : Parcelable.Creator<DimensionData> {

            override fun createFromParcel(parcel: Parcel): DimensionData {
                return when (parcel.readInt()) {
                    1 -> ResourceDimension.valueOf(parcel.readInt())
                    2 -> DynamicDimension.valueOf(parcel.readFloat())
                    else -> throw IllegalArgumentException("Unknown type")
                }
            }

            override fun newArray(size: Int): Array<DimensionData?> {
                return arrayOfNulls(size)
            }
        }
    }
}

class ResourceDimension private constructor(
    @DimenRes val default: Int
) : DimensionData() {

    companion object {

        fun valueOf(@DimenRes default: Int): ResourceDimension {
            return ResourceDimension(default = default)
        }
    }
}

class DynamicDimension private constructor(
    val default: Float
) : DimensionData() {

    companion object {

        fun valueOf(default: Float): DynamicDimension {
            return DynamicDimension(default = default)
        }
    }
}
