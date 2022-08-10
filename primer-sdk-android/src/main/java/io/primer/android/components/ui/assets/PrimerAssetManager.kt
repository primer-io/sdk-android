package io.primer.android.components.ui.assets

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.configuration.models.getImageAsset
import io.primer.android.infrastructure.files.ImagesFileProvider

internal object PrimerAssetManager {

    @DrawableRes
    fun getAsset(
        paymentMethodType: String,
        assetType: ImageType,
    ): Int? {
        val brand = PaymentMethodType.safeValueOf(paymentMethodType).brand

        val resourceId = when (assetType) {
            ImageType.ICON -> brand.iconResId
            ImageType.LOGO -> brand.logoResId
        }
        return when (resourceId) {
            0 -> null
            else -> resourceId
        }
    }

    fun getAsset(
        context: Context,
        paymentMethodType: String,
        imageColor: ImageColor,
    ): Drawable? {
        val fileProvider = ImagesFileProvider(context)
        val cachedDrawable =
            Drawable.createFromPath(
                fileProvider.getFile("${paymentMethodType}_$imageColor".lowercase()).absolutePath
            )
        val localResDrawableId =
            PaymentMethodType.safeValueOf(paymentMethodType).brand.getImageAsset(imageColor)
        return when {
            cachedDrawable != null -> cachedDrawable
            localResDrawableId != 0 -> ContextCompat.getDrawable(
                context,
                localResDrawableId
            )
            else -> null
        }
    }
}
