package io.primer.android.components.ui.assets

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

data class PrimerPaymentMethodAsset(
    val paymentMethodType: String,
    val paymentMethodLogo: PrimerPaymentMethodLogo,
    val paymentMethodBackgroundColor: PrimerPaymentMethodBackgroundColor
)

data class PrimerPaymentMethodLogo(
    val colored: Drawable?,
    val light: Drawable?,
    val dark: Drawable?
)

data class PrimerPaymentMethodBackgroundColor(
    @ColorInt val colored: Int?,
    @ColorInt val light: Int?,
    @ColorInt val dark: Int?
)
