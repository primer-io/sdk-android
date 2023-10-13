package io.primer.android.components.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager
import io.primer.android.components.ui.extensions.get
import io.primer.android.data.configuration.models.IconPosition
import io.primer.android.databinding.PrimerPaymentMethodTextButtonBinding
import io.primer.android.payment.config.TextDisplayMetadata
import io.primer.android.payment.utils.ButtonViewHelper
import io.primer.android.ui.settings.PrimerTheme

internal class DynamicPaymentMethodTextViewCreator(
    private val theme: PrimerTheme,
    private val displayMetadata: TextDisplayMetadata
) : PaymentMethodViewCreator {

    override fun create(context: Context, container: ViewGroup?): View {
        val binding = PrimerPaymentMethodTextButtonBinding.inflate(
            LayoutInflater.from(context),
            container,
            false
        )
        binding.apply {
            val content = ButtonViewHelper.generateButtonContent(context, theme, displayMetadata)
            displayMetadata.backgroundColor?.let {
                val splash = theme.splashColor.getColor(context, theme.isDarkMode)
                val rippleColor = ColorStateList.valueOf(splash)
                paymentMethodParent.background = RippleDrawable(rippleColor, content, null)
            }
            paymentMethodParent.contentDescription = displayMetadata.name
            paymentMethodButtonText.text = displayMetadata.text
            paymentMethodButtonText.setTextColor(Color.parseColor(displayMetadata.textColor))
            val paymentMethodAsset = displayMetadata.imageColor?.let {
                PrimerHeadlessUniversalCheckoutAssetsManager.getPaymentMethodAsset(
                    context,
                    displayMetadata.paymentMethodType
                ).paymentMethodLogo.get(it)
            }
            displayMetadata.iconPosition?.apply {
                paymentMethodButtonText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    if (this == IconPosition.START) paymentMethodAsset else null,
                    if (this == IconPosition.ABOVE) paymentMethodAsset else null,
                    if (this == IconPosition.END) paymentMethodAsset else null,
                    if (this == IconPosition.BELOW) paymentMethodAsset else null
                )
            }
        }
        return binding.root
    }
}
