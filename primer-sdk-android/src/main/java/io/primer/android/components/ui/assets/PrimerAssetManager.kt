package io.primer.android.components.ui.assets

import androidx.annotation.DrawableRes
import io.primer.android.model.dto.PrimerPaymentMethodType

internal object PrimerAssetManager {

    @DrawableRes
    fun getAsset(
        paymentMethodType: PrimerPaymentMethodType,
        assetType: ImageType,
    ): Int? {
        val resourceId = when (assetType) {
            ImageType.ICON -> paymentMethodType.brand.iconResId
            ImageType.LOGO -> paymentMethodType.brand.logoResId
        }
        return when (resourceId) {
            0 -> null
            else -> resourceId
        }
    }
}
