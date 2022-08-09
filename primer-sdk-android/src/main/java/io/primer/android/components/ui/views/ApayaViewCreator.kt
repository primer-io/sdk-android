package io.primer.android.components.ui.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.primer.android.R
import io.primer.android.databinding.PaymentMethodButtonPayMobileBinding
import io.primer.android.ui.settings.PrimerTheme

internal class ApayaViewCreator(private val theme: PrimerTheme) : PaymentMethodViewCreator {
    override fun create(context: Context, container: ViewGroup?): View {
        val binding = PaymentMethodButtonPayMobileBinding.inflate(
            LayoutInflater.from(context),
            container,
            false
        )
        val text = binding.payMobilePreviewButton
        val drawable = ContextCompat.getDrawable(
            context,
            R.drawable.ic_logo_apaya
        )

        text.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

        text.setTextColor(
            theme.paymentMethodButton.text.defaultColor.getColor(
                context,
                theme.isDarkMode
            )
        )

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
