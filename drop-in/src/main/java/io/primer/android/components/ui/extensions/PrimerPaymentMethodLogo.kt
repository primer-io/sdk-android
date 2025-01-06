package io.primer.android.components.ui.extensions

import io.primer.android.components.ui.assets.PrimerAsset
import io.primer.android.displayMetadata.domain.model.ImageColor

internal fun PrimerAsset.get(imageColor: ImageColor) =
    when (imageColor) {
        ImageColor.COLORED -> colored
        ImageColor.DARK -> dark
        ImageColor.LIGHT -> light
    }
