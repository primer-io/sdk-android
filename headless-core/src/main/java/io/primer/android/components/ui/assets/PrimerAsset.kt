package io.primer.android.components.ui.assets

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import io.primer.android.configuration.data.model.CardNetwork

interface PrimerAsset {
    val colored: Drawable?
    val light: Drawable?
    val dark: Drawable?
}

sealed interface PrimerPaymentMethodResource {
    val paymentMethodType: String
    val paymentMethodName: String
}

data class PrimerPaymentMethodAsset(
    override val paymentMethodType: String,
    override val paymentMethodName: String,
    val paymentMethodLogo: PrimerAsset,
    val paymentMethodBackgroundColor: PrimerPaymentMethodBackgroundColor,
) : PrimerPaymentMethodResource

data class PrimerPaymentMethodNativeView(
    override val paymentMethodType: String,
    override val paymentMethodName: String,
    val createView: (Context) -> View,
) : PrimerPaymentMethodResource

@Deprecated(message = "This class is deprecated.", ReplaceWith("Use PrimerAsset"))
data class PrimerPaymentMethodLogo(
    override val colored: Drawable?,
    override val light: Drawable?,
    override val dark: Drawable?,
) : PrimerAsset

data class PrimerPaymentMethodBackgroundColor(
    @ColorInt val colored: Int?,
    @ColorInt val light: Int?,
    @ColorInt val dark: Int?,
)

data class PrimerCardNetworkAsset(
    val cardNetwork: CardNetwork.Type,
    val displayName: String,
    val cardImage: Drawable?,
)
