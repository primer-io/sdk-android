package io.primer.android.payment.config

import android.content.Context
import io.primer.android.R
import io.primer.android.components.ui.assets.ImageColor
import io.primer.android.data.configuration.models.IconPosition
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.payments.displayMetadata.models.PaymentMethodImplementation

internal data class TextDisplayMetadata(
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

internal fun PaymentMethodImplementation.toTextDisplayMetadata(isDarkMode: Boolean, context: Context) =
    TextDisplayMetadata(
        name = name,
        paymentMethodType = paymentMethodType,
        backgroundColor = buttonMetadata?.backgroundColor?.getColor(isDarkMode),
        borderColor = buttonMetadata?.borderColor?.getColor(isDarkMode),
        borderWidth = buttonMetadata?.borderWidth?.getBorderWidth(isDarkMode),
        cornerRadius = buttonMetadata?.cornerRadius,
        text = if (paymentMethodType == PaymentMethodType.STRIPE_ACH.name) {
            context.getString(R.string.pay_with_ach)
        } else {
            buttonMetadata?.text
        },
        textColor = buttonMetadata?.textColor?.getColor(isDarkMode),
        imageColor = buttonMetadata?.iconDisplayMetadata?.getImageColor(isDarkMode),
        iconPosition = buttonMetadata?.iconPosition
    )
