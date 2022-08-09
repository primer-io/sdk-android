package io.primer.android.payment.config

import io.primer.android.components.ui.assets.ImageColor
import io.primer.android.data.configuration.models.IconPosition
import io.primer.android.domain.payments.displayMetadata.models.PaymentMethodImplementation

internal class TextDisplayMetadata(
    override val name: String?,
    override val paymentMethodType: String,
    override val backgroundColor: String?,
    override val borderColor: String?,
    override val borderWidth: Float?,
    override val cornerRadius: Float?,
    val text: String?,
    val textColor: String?,
    val imageColor: ImageColor?,
    val iconPosition: IconPosition?
) : BaseDisplayMetadata(
    DisplayMetadataType.TEXT,
    name,
    paymentMethodType,
    backgroundColor,
    borderColor,
    borderWidth,
    cornerRadius
)

internal fun PaymentMethodImplementation.toTextDisplayMetadata(isDarkMode: Boolean) =
    TextDisplayMetadata(
        name,
        paymentMethodType,
        buttonMetadata?.backgroundColor?.getColor(isDarkMode),
        buttonMetadata?.borderColor?.getColor(isDarkMode),
        buttonMetadata?.borderWidth?.getBorderWidth(isDarkMode),
        buttonMetadata?.cornerRadius,
        buttonMetadata?.text,
        buttonMetadata?.textColor?.getColor(isDarkMode),
        buttonMetadata?.iconDisplayMetadata?.getImageColor(isDarkMode),
        buttonMetadata?.iconPosition
    )
