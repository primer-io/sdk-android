package io.primer.android.components.ui.assets

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.configuration.models.getImageAsset
import io.primer.android.infrastructure.files.ImagesFileProvider
import io.primer.android.ui.CardNetwork

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

    @DrawableRes
    fun getAsset(
        cardNetwork: CardNetwork.Type,
    ): Int {
        return when (cardNetwork) {
            CardNetwork.Type.VISA -> R.drawable.ic_visa_card
            CardNetwork.Type.MASTERCARD -> R.drawable.ic_mastercard_card
            CardNetwork.Type.AMEX -> R.drawable.ic_amex_card
            CardNetwork.Type.DISCOVER -> R.drawable.ic_discover_card
            CardNetwork.Type.JCB -> R.drawable.ic_jcb_card
            else -> R.drawable.ic_generic_card
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
