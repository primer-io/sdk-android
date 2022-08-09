package io.primer.android.data.payments.displayMetadata.model

import io.primer.android.components.ui.assets.ImageColor

internal data class IconDisplayMetadata(
    val imageColor: ImageColor,
    val url: String? = null,
    val filePath: String? = null,
    val iconResId: Int = 0
)
