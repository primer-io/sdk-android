package io.primer.android.ui.settings

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DimenRes

enum class DimensionType(val value: Int) {
    RESOURCE_DIMENSION(1),
    DYNAMIC_DIMENSION(2);

    companion object {
        fun fromValue(value: Int): DimensionType {
            return values().first { it.value == value }
        }
    }
}

sealed class DimensionData(private val dimensionType: DimensionType) : Parcelable {

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
                parcel.writeInt(dimensionType.value)
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
                return when (DimensionType.fromValue(parcel.readInt())) {
                    DimensionType.RESOURCE_DIMENSION -> ResourceDimension.valueOf(parcel.readInt())
                    DimensionType.DYNAMIC_DIMENSION -> DynamicDimension.valueOf(parcel.readFloat())
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
) : DimensionData(DimensionType.RESOURCE_DIMENSION) {

    companion object {

        fun valueOf(@DimenRes default: Int): ResourceDimension {
            return ResourceDimension(default = default)
        }
    }
}

class DynamicDimension private constructor(
    val default: Float
) : DimensionData(DimensionType.DYNAMIC_DIMENSION) {

    companion object {

        fun valueOf(default: Float): DynamicDimension {
            return DynamicDimension(default = default)
        }
    }
}
