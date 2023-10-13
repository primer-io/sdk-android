package io.primer.android.payment.config

import io.primer.android.components.ui.assets.ImageColor
import io.primer.android.data.payments.displayMetadata.model.IconDisplayMetadata
import io.primer.android.domain.payments.displayMetadata.models.PaymentMethodImplementation

abstract class BaseDisplayMetadata(
    open val type: DisplayMetadataType,
    open val name: String?,
    open val paymentMethodType: String,
    open val backgroundColor: String?,
    open val borderColor: String?,
    open val borderWidth: Float?,
    open val cornerRadius: Float?
) {
    enum class DisplayMetadataType {
        IMAGE,
        TEXT
    }
}

internal fun PaymentMethodImplementation.ButtonMetadata.ColorMetadata.getColor(
    isDarkMode: Boolean
) = when {
    dark.isNullOrBlank().not() && isDarkMode -> dark
    colored.isNullOrBlank().not() -> colored
    light.isNullOrBlank().not() -> light
    else -> null
}

internal fun PaymentMethodImplementation.ButtonMetadata.BorderWidthMetadata.getBorderWidth(
    isDarkMode: Boolean
) = when {
    dark != null && isDarkMode -> dark
    colored != null -> colored
    light != null -> light
    else -> null
}

internal fun List<IconDisplayMetadata>.getImageColor(
    isDarkMode: Boolean
) = when {
    find { it.imageColor == ImageColor.DARK } != null && isDarkMode -> ImageColor.DARK
    find { it.imageColor == ImageColor.COLORED } != null -> ImageColor.COLORED
    find { it.imageColor == ImageColor.LIGHT } != null -> ImageColor.LIGHT
    else -> null
}
