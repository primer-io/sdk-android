package io.primer.android.components.ui.assets

import android.content.Context
import androidx.annotation.DrawableRes
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.presentation.assets.DefaultAssetsHeadlessDelegate
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.resolve
import io.primer.android.ui.CardNetwork

class PrimerHeadlessUniversalCheckoutAssetsManager private constructor() : DISdkComponent {

    @Throws(SdkUninitializedException::class)
    internal fun getPaymentMethodAssets(context: Context): List<PrimerPaymentMethodAsset> {
        return getAssetsDelegate().getCurrentPaymentMethods().map {
            getPaymentMethodAsset(context, it)
        }
    }

    @Throws(SdkUninitializedException::class)
    internal fun getPaymentMethodAsset(
        context: Context,
        paymentMethodType: String
    ): PrimerPaymentMethodAsset {
        val delegate = getAssetsDelegate()
        return PrimerPaymentMethodAsset(
            paymentMethodType,
            delegate.getPaymentMethodName(paymentMethodType),
            PrimerPaymentMethodLogo(
                delegate.getPaymentMethodLogo(
                    context,
                    paymentMethodType,
                    ImageColor.COLORED
                ),
                delegate.getPaymentMethodLogo(
                    context,
                    paymentMethodType,
                    ImageColor.LIGHT
                ),
                delegate.getPaymentMethodLogo(
                    context,
                    paymentMethodType,
                    ImageColor.DARK
                )
            ),
            PrimerPaymentMethodBackgroundColor(
                delegate.getPaymentMethodBackgroundColor(paymentMethodType, ImageColor.COLORED),
                delegate.getPaymentMethodBackgroundColor(paymentMethodType, ImageColor.LIGHT),
                delegate.getPaymentMethodBackgroundColor(paymentMethodType, ImageColor.DARK)
            )
        )
    }

    @DrawableRes
    internal fun getCardNetworkImage(
        cardNetwork: CardNetwork.Type
    ): Int {
        return getAssetsDelegate().getCardNetworkImage(cardNetwork)
    }

    internal fun getCardNetworkAsset(
        context: Context,
        cardNetwork: CardNetwork.Type
    ): PrimerCardNetworkAsset {
        return getAssetsDelegate().getCardNetworkAsset(context, cardNetwork)
    }

    // this will be added in subsequent release
    internal fun getCardNetworkAssets(
        context: Context,
        cardNetworks: List<CardNetwork.Type>
    ): List<PrimerCardNetworkAsset> {
        return getAssetsDelegate().getCardNetworkAssets(context, cardNetworks)
    }

    private fun getAssetsDelegate(): DefaultAssetsHeadlessDelegate = resolve()

    companion object {

        @JvmStatic
        private val assetManager by lazy { PrimerHeadlessUniversalCheckoutAssetsManager() }

        /**
         * Returns all [PrimerPaymentMethodAsset] tied to current client session.
         * @throws SdkUninitializedException
         */
        @Throws(SdkUninitializedException::class)
        @JvmStatic
        fun getPaymentMethodAssets(context: Context): List<PrimerPaymentMethodAsset> =
            assetManager.getPaymentMethodAssets(context)

        /**
         * Returns [PrimerPaymentMethodAsset] for a given [paymentMethodType].
         * @throws SdkUninitializedException
         */
        @Throws(SdkUninitializedException::class)
        @JvmStatic
        fun getPaymentMethodAsset(
            context: Context,
            paymentMethodType: String
        ): PrimerPaymentMethodAsset = assetManager.getPaymentMethodAsset(context, paymentMethodType)

        @Deprecated(
            message = "This method is deprecated.",
            replaceWith = ReplaceWith("getCardNetworkAssets(context, cardNetwork)")
        )
        @DrawableRes
        @JvmStatic
        fun getCardNetworkImage(
            cardNetwork: CardNetwork.Type
        ): Int = assetManager.getCardNetworkImage(cardNetwork)

        /**
         * This method returns a card asset for the specified card network.
         */
        @JvmStatic
        fun getCardNetworkAsset(
            context: Context,
            cardNetwork: CardNetwork.Type
        ): PrimerCardNetworkAsset = assetManager.getCardNetworkAsset(context, cardNetwork)
    }
}
