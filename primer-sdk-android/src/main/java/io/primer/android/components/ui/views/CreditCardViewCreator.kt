package io.primer.android.components.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.primer.android.PrimerSessionIntent
import io.primer.android.R
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.databinding.PaymentMethodButtonCardBinding
import io.primer.android.payment.utils.ButtonViewHelper

internal class CreditCardViewCreator(
    private val config: PrimerConfig
) : PaymentMethodViewCreator {
    override fun create(context: Context, container: ViewGroup?): View {
        val binding = PaymentMethodButtonCardBinding.inflate(
            LayoutInflater.from(context),
            container,
            false
        )
        val theme = config.settings.uiOptions.theme

        val content = ButtonViewHelper.generateButtonContent(context, theme)
        val splash = theme.splashColor.getColor(context, theme.isDarkMode)
        val rippleColor = ColorStateList.valueOf(splash)
        binding.cardPreviewButton.background = RippleDrawable(rippleColor, content, null)

        val text = binding.cardPreviewButtonText
        val drawable = ContextCompat.getDrawable(
            context,
            R.drawable.ic_logo_credit_card
        )

        text.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

        text.setTextColor(
            theme.paymentMethodButton.text.defaultColor.getColor(
                context,
                theme.isDarkMode
            )
        )

        text.text = when (config.paymentMethodIntent) {
            PrimerSessionIntent.CHECKOUT -> context.getString(R.string.pay_by_card)
            PrimerSessionIntent.VAULT ->
                context.getString(R.string.credit_debit_card)
        }

        val icon = text.compoundDrawables

        DrawableCompat.setTint(
            DrawableCompat.wrap(icon[0]),
            theme.paymentMethodButton.text.defaultColor.getColor(
                context,
                theme.isDarkMode
            )
        )

        return binding.root
    }
}
