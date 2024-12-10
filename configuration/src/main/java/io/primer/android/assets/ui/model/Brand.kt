package io.primer.android.assets.ui.model

import androidx.annotation.DrawableRes
import io.primer.android.displayMetadata.domain.model.ImageColor

interface Brand {
    @get:DrawableRes
    val iconResId: Int

    @get:DrawableRes
    val logoResId: Int
        get() = 0

    @get:DrawableRes
    val iconLightResId: Int
        get() = iconResId

    @get:DrawableRes
    val iconDarkResId: Int
        get() = iconResId
}

internal object UnknownBrand : Brand {
    override val iconResId: Int
        get() = 0
}

fun Brand.getImageAsset(imageColor: ImageColor) = when (imageColor) {
    ImageColor.COLORED -> iconResId
    ImageColor.DARK -> iconDarkResId
    ImageColor.LIGHT -> iconLightResId
}
