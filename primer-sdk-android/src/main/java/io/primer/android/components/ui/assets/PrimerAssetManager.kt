package io.primer.android.components.ui.assets

import android.content.Context

internal object PrimerAssetManager {

    fun getAsset(
        context: Context,
        brand: Brand,
        assetType: ImageType,
    ): Int? {
        val resources = context.resources
        val resourceName = when (assetType) {
            ImageType.ICON -> "ic_logo_${brand.brandName}"
            ImageType.LOGO -> "ic_logo_${brand.brandName}_square"
        }
        val resourceId: Int = resources.getIdentifier(
            resourceName, "drawable",
            context.packageName
        )
        return when (resourceId) {
            0 -> null
            else -> resourceId
        }
    }
}
