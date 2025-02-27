package io.primer.android.components.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.TextUtilsCompat
import io.primer.android.PrimerSessionIntent
import io.primer.android.R
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.databinding.PrimerPaymentMethodButtonCardBinding
import io.primer.android.payment.utils.ButtonViewHelper
import java.util.Locale

internal class CreditCardViewCreator(
    private val config: PrimerConfig,
) : PaymentMethodViewCreator {
    override fun create(
        context: Context,
        container: ViewGroup?,
    ): View {
        val binding =
            PrimerPaymentMethodButtonCardBinding.inflate(
                LayoutInflater.from(context),
                container,
                false,
            )
        val theme = config.settings.uiOptions.theme

        val content = ButtonViewHelper.generateButtonContent(context, theme)
        val splash = theme.splashColor.getColor(context, theme.isDarkMode)
        val rippleColor = ColorStateList.valueOf(splash)
        binding.cardPreviewButton.background = RippleDrawable(rippleColor, content, null)

        val text = binding.cardPreviewButtonText
        val drawable =
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_logo_credit_card,
            )
        val layoutDirection = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())
        val isLeftToRight = layoutDirection == View.LAYOUT_DIRECTION_LTR
        text.setCompoundDrawablesWithIntrinsicBounds(
            if (isLeftToRight) drawable else null,
            null,
            if (isLeftToRight) null else drawable,
            null,
        )

        text.setTextColor(
            theme.paymentMethodButton.text.defaultColor.getColor(
                context,
                theme.isDarkMode,
            ),
        )

        text.text =
            when (config.paymentMethodIntent) {
                PrimerSessionIntent.CHECKOUT -> context.getString(R.string.pay_by_card)
                PrimerSessionIntent.VAULT ->
                    context.getString(R.string.credit_debit_card)
            }

        val icon = text.compoundDrawables

        DrawableCompat.setTint(
            DrawableCompat.wrap(icon.first { it != null }),
            theme.paymentMethodButton.text.defaultColor.getColor(
                context,
                theme.isDarkMode,
            ),
        )

        return binding.root
    }
}
