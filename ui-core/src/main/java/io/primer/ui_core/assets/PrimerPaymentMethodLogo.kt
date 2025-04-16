package io.primer.ui_core.assets

import io.primer.android.components.ui.assets.PrimerAsset
import io.primer.android.displayMetadata.domain.model.ImageColor

fun PrimerAsset.get(imageColor: ImageColor) =
    when (imageColor) {
        ImageColor.COLORED -> colored
        ImageColor.DARK -> dark
        ImageColor.LIGHT -> light
    }
