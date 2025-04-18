package io.primer.ui.core.assets

import android.content.Context
import android.graphics.drawable.Drawable
import io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager
import io.primer.android.components.ui.assets.PrimerPaymentMethodResource
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.displayMetadata.domain.model.ImageColor

interface AssetsManager {
    fun getPaymentMethodImage(
        context: Context,
        paymentMethodType: String,
        imageColor: ImageColor,
    ): Drawable?

    fun getCardNetworkImage(
        context: Context,
        network: CardNetwork.Type,
    ): Drawable?

    fun getPaymentMethodResource(
        context: Context,
        paymentMethodType: String,
    ): PrimerPaymentMethodResource
}

class DefaultPrimerAssetsManager(
    private val headlessUniversalCheckoutAssetsManager: PrimerHeadlessUniversalCheckoutAssetsManager.Companion,
) : AssetsManager {
    override fun getPaymentMethodImage(
        context: Context,
        paymentMethodType: String,
        imageColor: ImageColor,
    ): Drawable? {
        return headlessUniversalCheckoutAssetsManager.getPaymentMethodAsset(
            context = context,
            paymentMethodType = paymentMethodType,
        ).paymentMethodLogo.get(imageColor)
    }

    override fun getCardNetworkImage(
        context: Context,
        network: CardNetwork.Type,
    ): Drawable? {
        return headlessUniversalCheckoutAssetsManager.getCardNetworkAsset(
            context = context,
            cardNetwork = network,
        ).cardImage
    }

    override fun getPaymentMethodResource(
        context: Context,
        paymentMethodType: String,
    ): PrimerPaymentMethodResource {
        return headlessUniversalCheckoutAssetsManager.getPaymentMethodResource(
            context = context,
            paymentMethodType = paymentMethodType,
        )
    }
}
