package io.primer.android.payment.config

import io.primer.android.components.ui.assets.ImageColor
import io.primer.android.domain.payments.displayMetadata.models.PaymentMethodImplementation

internal data class ImageDisplayMetadata(
    override val name: String?,
    override val paymentMethodType: String,
    override val backgroundColor: String?,
    override val borderColor: String?,
    override val borderWidth: Float?,
    override val cornerRadius: Float?,
    val imageColor: ImageColor?,
) : BaseDisplayMetadata(
    DisplayMetadataType.IMAGE,
    name,
    paymentMethodType,
    backgroundColor,
    borderColor,
    borderWidth,
    cornerRadius
)

internal fun PaymentMethodImplementation.toImageDisplayMetadata(isDarkMode: Boolean) =
    ImageDisplayMetadata(
        name,
        paymentMethodType,
        buttonMetadata?.backgroundColor?.getColor(isDarkMode),
        buttonMetadata?.borderColor?.getColor(isDarkMode),
        buttonMetadata?.borderWidth?.getBorderWidth(isDarkMode),
        buttonMetadata?.cornerRadius,
        buttonMetadata?.iconDisplayMetadata?.getImageColor(isDarkMode)
    )
