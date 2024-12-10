package io.primer.android.components.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.databinding.PrimerPaymentMethodImageButtonBinding
import io.primer.android.payment.config.ImageDisplayMetadata
import io.primer.android.payment.utils.ButtonViewHelper
import io.primer.android.paymentMethods.core.ui.assets.AssetsManager

internal class DynamicPaymentMethodImageViewCreator(
    private val theme: PrimerTheme,
    private val displayMetadata: ImageDisplayMetadata,
    private val assetsManager: AssetsManager
) : PaymentMethodViewCreator {

    override fun create(context: Context, container: ViewGroup?): View {
        val binding = PrimerPaymentMethodImageButtonBinding.inflate(
            LayoutInflater.from(context),
            container,
            false
        )
        val paymentMethodAsset = displayMetadata.imageColor?.let { imageColor ->
            assetsManager.getPaymentMethodImage(
                context = context,
                paymentMethodType = displayMetadata.paymentMethodType,
                imageColor = imageColor
            )
        }
        binding.apply {
            val content = ButtonViewHelper.generateButtonContent(context, theme, displayMetadata)
            displayMetadata.backgroundColor?.let {
                val splash = theme.splashColor.getColor(context, theme.isDarkMode)
                val rippleColor = ColorStateList.valueOf(splash)
                paymentMethodParent.background = RippleDrawable(rippleColor, content, null)
            }

            paymentMethodParent.contentDescription = context.getString(
                R.string.primer_payment_method_button_content_description,
                displayMetadata.name
            )
            paymentMethodIcon.setImageDrawable(paymentMethodAsset)
        }
        return binding.root
    }
}
