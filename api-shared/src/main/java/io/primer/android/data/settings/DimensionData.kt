package io.primer.android.data.settings

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DimenRes
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

enum class DimensionType(val value: Int) {
    RESOURCE_DIMENSION(1),
    DYNAMIC_DIMENSION(2);

    companion object {
        fun fromValue(value: Int): DimensionType {
            return DimensionType.entries.first { it.value == value }
        }
    }
}

sealed class DimensionData(private val dimensionType: DimensionType) : Parcelable, JSONObjectSerializable {

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

        private const val DIMENSION_TYPE_FIELD = "dimensionType"
        private const val DEFAULT_VALUE_FIELD = "defaultValue"

        @JvmField
        val serializer = JSONObjectSerializer<DimensionData> { t ->
            JSONObject().apply {
                put(DIMENSION_TYPE_FIELD, t.dimensionType)
                when (t) {
                    is ResourceDimension -> put(DEFAULT_VALUE_FIELD, t.default)
                    is DynamicDimension -> put(DEFAULT_VALUE_FIELD, t.default)
                }
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
