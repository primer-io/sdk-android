package io.primer.android.payment.config

import io.primer.android.components.assets.displayMetadata.models.PaymentMethodImplementation
import io.primer.android.displayMetadata.domain.model.IconDisplayMetadata
import io.primer.android.displayMetadata.domain.model.ImageColor

abstract class BaseDisplayMetadata(
    open val type: DisplayMetadataType,
    open val name: String?,
    open val paymentMethodType: String,
    open val backgroundColor: String?,
    open val borderColor: String?,
    open val borderWidth: Float?,
    open val cornerRadius: Float?,
) {
    enum class DisplayMetadataType {
        IMAGE,
        TEXT,
    }
}

internal fun PaymentMethodImplementation.ButtonMetadata.ColorMetadata.getColor(isDarkMode: Boolean) =
    when {
        dark.isNullOrBlank().not() && isDarkMode -> dark
        colored.isNullOrBlank().not() -> colored
        light.isNullOrBlank().not() -> light
        else -> null
    }

internal fun PaymentMethodImplementation.ButtonMetadata.BorderWidthMetadata.getBorderWidth(isDarkMode: Boolean) =
    when {
        dark != null && isDarkMode -> dark
        colored != null -> colored
        light != null -> light
        else -> null
    }

internal fun List<IconDisplayMetadata>.getImageColor(isDarkMode: Boolean) =
    when {
        any { it.imageColor == ImageColor.DARK } && isDarkMode -> ImageColor.DARK
        any { it.imageColor == ImageColor.COLORED } -> ImageColor.COLORED
        any { it.imageColor == ImageColor.LIGHT } -> ImageColor.LIGHT
        else -> null
    }
