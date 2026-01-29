package io.primer.android.components.ui.assets

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.graphics.toColorInt
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.assets.DefaultAssetsHeadlessDelegate
import io.primer.android.components.assets.displayMetadata.models.PaymentMethodImplementation
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.IconPosition
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.displayMetadata.domain.model.ImageColor

class PrimerHeadlessUniversalCheckoutAssetsManager private constructor() : DISdkComponent {
    @Throws(SdkUninitializedException::class)
    internal fun getPaymentMethodAssets(context: Context): List<PrimerPaymentMethodAsset> {
        return getAssetsDelegate().getCurrentPaymentMethods().map {
            getPaymentMethodAsset(context, it)
        }
    }

    @Throws(SdkUninitializedException::class)
    internal fun getPaymentMethodResources(context: Context): List<PrimerPaymentMethodResource> {
        return getAssetsDelegate().getCurrentPaymentMethods().map {
            this.getPaymentMethodResource(context, it)
        }
    }

    @Throws(SdkUninitializedException::class)
    internal fun getPaymentMethodAsset(
        context: Context,
        paymentMethodType: String,
    ): PrimerPaymentMethodAsset {
        val delegate = getAssetsDelegate()
        val buttonMetadata = delegate.getPaymentMethodButtonMetadata(paymentMethodType)
        return PrimerPaymentMethodAsset(
            paymentMethodType = paymentMethodType,
            paymentMethodName = delegate.getPaymentMethodName(paymentMethodType),
            paymentMethodLogo = PrimerPaymentMethodLogo(
                delegate.getPaymentMethodLogo(
                    context,
                    paymentMethodType,
                    ImageColor.COLORED,
                ),
                delegate.getPaymentMethodLogo(
                    context,
                    paymentMethodType,
                    ImageColor.LIGHT,
                ),
                delegate.getPaymentMethodLogo(
                    context,
                    paymentMethodType,
                    ImageColor.DARK,
                ),
            ),
            paymentMethodBackgroundColor = PrimerPaymentMethodBackgroundColor(
                delegate.getPaymentMethodBackgroundColor(paymentMethodType, ImageColor.COLORED),
                delegate.getPaymentMethodBackgroundColor(paymentMethodType, ImageColor.LIGHT),
                delegate.getPaymentMethodBackgroundColor(paymentMethodType, ImageColor.DARK),
            ),
            text = buttonMetadata?.text,
            textColor = buttonMetadata?.textColor?.toPrimerColor(),
            borderColor = buttonMetadata?.borderColor?.toPrimerColor(),
            borderWidth = buttonMetadata?.borderWidth?.toPrimerBorderWidth(),
            cornerRadius = buttonMetadata?.cornerRadius,
            iconPosition = buttonMetadata?.iconPosition.toPrimerIconPosition(),
        )
    }

    private fun PaymentMethodImplementation.ButtonMetadata.ColorMetadata.toPrimerColor(): PrimerPaymentMethodColor {
        return PrimerPaymentMethodColor(
            colored = colored?.let { runCatching { it.toColorInt() }.getOrNull() },
            light = light?.let { runCatching { it.toColorInt() }.getOrNull() },
            dark = dark?.let { runCatching { it.toColorInt() }.getOrNull() },
        )
    }

    private fun PaymentMethodImplementation.ButtonMetadata.BorderWidthMetadata.toPrimerBorderWidth(): PrimerPaymentMethodBorderWidth {
        return PrimerPaymentMethodBorderWidth(
            colored = colored,
            light = light,
            dark = dark,
        )
    }

    private fun IconPosition?.toPrimerIconPosition(): PrimerIconPosition {
        return when (this) {
            IconPosition.START -> PrimerIconPosition.START
            IconPosition.END -> PrimerIconPosition.END
            IconPosition.ABOVE -> PrimerIconPosition.ABOVE
            IconPosition.BELOW -> PrimerIconPosition.BELOW
            null -> PrimerIconPosition.START
        }
    }

    @Throws(SdkUninitializedException::class)
    internal fun getPaymentMethodResource(
        context: Context,
        paymentMethodType: String,
    ): PrimerPaymentMethodResource {
        val delegate = getAssetsDelegate()
        return delegate.getPaymentMethodViewProvider(context = context, paymentMethodType)?.let { viewProvider ->
            PrimerPaymentMethodNativeView(
                paymentMethodType,
                delegate.getPaymentMethodName(paymentMethodType),
                viewProvider,
            )
        } ?: getPaymentMethodAsset(context, paymentMethodType)
    }

    @DrawableRes
    internal fun getCardNetworkImage(cardNetwork: CardNetwork.Type): Int {
        return getAssetsDelegate().getCardNetworkImage(cardNetwork)
    }

    internal fun getCardNetworkAsset(
        context: Context,
        cardNetwork: CardNetwork.Type,
    ): PrimerCardNetworkAsset {
        return getAssetsDelegate().getCardNetworkAsset(context, cardNetwork)
    }

    // this will be added in subsequent release
    internal fun getCardNetworkAssets(
        context: Context,
        cardNetworks: List<CardNetwork.Type>,
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
        @Deprecated(
            message = "This method is deprecated.",
            replaceWith = ReplaceWith("getPaymentMethodResources(context)"),
        )
        @Throws(SdkUninitializedException::class)
        @JvmStatic
        fun getPaymentMethodAssets(context: Context): List<PrimerPaymentMethodAsset> =
            assetManager.getPaymentMethodAssets(context)

        /**
         * Returns all [PrimerPaymentMethodResource] tied to current client session.
         * @throws SdkUninitializedException
         */
        @Throws(SdkUninitializedException::class)
        @JvmStatic
        fun getPaymentMethodResources(context: Context): List<PrimerPaymentMethodResource> =
            assetManager.getPaymentMethodResources(context)

        /**
         * Returns [PrimerPaymentMethodAsset] for a given [paymentMethodType].
         * @throws SdkUninitializedException
         */
        @Deprecated(
            message = "This method is deprecated.",
            replaceWith = ReplaceWith("getPaymentMethodResource(context, paymentMethodType)"),
        )
        @Throws(SdkUninitializedException::class)
        @JvmStatic
        fun getPaymentMethodAsset(
            context: Context,
            paymentMethodType: String,
        ): PrimerPaymentMethodAsset = assetManager.getPaymentMethodAsset(context, paymentMethodType)

        /**
         * Returns a [PrimerPaymentMethodResource] for the specified payment method type.
         *
         * This method returns either:
         * - [PrimerPaymentMethodAsset] containing styling resources (logo, colors) for regular payment methods
         * - [PrimerPaymentMethodNativeView] containing a custom view creator for special payment methods (e.g. Google Pay)
         *
         * Example:
         * ```kotlin
         * val resource = getPaymentMethodResource(context, paymentMethodType)
         * when (resource) {
         *     is PrimerPaymentMethodAsset -> {
         *         setBackgroundColor(resource.paymentMethodBackgroundColor.colored)
         *         setImage(resource.paymentMethodLogo.colored)
         *     }
         *     is PrimerPaymentMethodNativeView -> {
         *         addView(resource.createView(context))
         *     }
         * }
         * ```
         *
         * @param context Android context
         * @param paymentMethodType Type identifier of the payment method (e.g. "GOOGLE_PAY")
         * @return [PrimerPaymentMethodResource] containing either styling resources or view creation capability
         * @throws SdkUninitializedException if SDK is not initialized
         */
        @Throws(SdkUninitializedException::class)
        @JvmStatic
        fun getPaymentMethodResource(
            context: Context,
            paymentMethodType: String,
        ): PrimerPaymentMethodResource = assetManager.getPaymentMethodResource(context, paymentMethodType)

        @Deprecated(
            message = "This method is deprecated.",
            replaceWith = ReplaceWith("getCardNetworkAssets(context, cardNetwork)"),
        )
        @DrawableRes
        @JvmStatic
        fun getCardNetworkImage(cardNetwork: CardNetwork.Type): Int = assetManager.getCardNetworkImage(cardNetwork)

        /**
         * This method returns a card asset for the specified card network.
         */
        @JvmStatic
        fun getCardNetworkAsset(
            context: Context,
            cardNetwork: CardNetwork.Type,
        ): PrimerCardNetworkAsset = assetManager.getCardNetworkAsset(context, cardNetwork)
    }
}
