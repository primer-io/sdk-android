package io.primer.android.components.ui.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.databinding.PaymentMethodButtonDirectDebitBinding
import io.primer.android.ui.settings.PrimerTheme

internal class GoCardlessViewCreator(private val theme: PrimerTheme) :
    PaymentMethodViewCreator {
    override fun create(context: Context, container: ViewGroup?): View {
        val binding = PaymentMethodButtonDirectDebitBinding.inflate(
            LayoutInflater.from(context),
            container,
            false
        )
        val text = binding.directDebitButtonText
        text.setTextColor(
            theme.paymentMethodButton.text.defaultColor.getColor(
                context,
                theme.isDarkMode
            )
        )

        return binding.root
    }
}
