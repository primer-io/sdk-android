package io.primer.android.ui.settings

import android.content.Context
import androidx.annotation.DimenRes

sealed class DimensionData {

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
}

class ResourceDimension private constructor(
    @DimenRes val default: Int,
) : DimensionData() {

    companion object {

        fun valueOf(@DimenRes default: Int): ResourceDimension {
            return ResourceDimension(default = default)
        }
    }
}

class DynamicDimension private constructor(
    val default: Float,
) : DimensionData() {

    companion object {

        fun valueOf(default: Float): DynamicDimension {
            return DynamicDimension(default = default)
        }
    }
}
