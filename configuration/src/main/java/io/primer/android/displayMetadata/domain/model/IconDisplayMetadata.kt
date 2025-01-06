package io.primer.android.displayMetadata.domain.model

data class IconDisplayMetadata(
    val imageColor: ImageColor,
    val url: String? = null,
    val filePath: String? = null,
    val iconResId: Int = 0,
)
