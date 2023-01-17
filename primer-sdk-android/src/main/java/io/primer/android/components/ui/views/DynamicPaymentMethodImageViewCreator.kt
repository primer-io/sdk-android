package io.primer.android.components.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager
import io.primer.android.components.ui.extensions.get
import io.primer.android.databinding.PrimerPaymentMethodImageButtonBinding
import io.primer.android.payment.config.ImageDisplayMetadata
import io.primer.android.payment.utils.ButtonViewHelper
import io.primer.android.ui.settings.PrimerTheme

internal class DynamicPaymentMethodImageViewCreator(
    private val theme: PrimerTheme,
    private val displayMetadata: ImageDisplayMetadata,
) : PaymentMethodViewCreator {

    override fun create(context: Context, container: ViewGroup?): View {
        val binding = PrimerPaymentMethodImageButtonBinding.inflate(
            LayoutInflater.from(context),
            container,
            false
        )
        val paymentMethodAsset = displayMetadata.imageColor?.let {
            PrimerHeadlessUniversalCheckoutAssetsManager.getPaymentMethodAsset(
                context,
                displayMetadata.paymentMethodType,
            ).paymentMethodLogo.get(it)
        }
        binding.apply {
            val content = ButtonViewHelper.generateButtonContent(context, theme, displayMetadata)
            displayMetadata.backgroundColor?.let {
                val splash = theme.splashColor.getColor(context, theme.isDarkMode)
                val rippleColor = ColorStateList.valueOf(splash)
                paymentMethodParent.background = RippleDrawable(rippleColor, content, null)
            }

            paymentMethodParent.contentDescription = displayMetadata.name
            paymentMethodIcon.setImageDrawable(paymentMethodAsset)
        }
        return binding.root
    }
}
