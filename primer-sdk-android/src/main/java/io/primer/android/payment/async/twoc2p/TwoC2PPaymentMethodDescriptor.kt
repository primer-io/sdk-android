package io.primer.android.payment.async.twoc2p

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodRemoteConfig
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.databinding.PaymentMethodButtonTwoc2pBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.settings.PrimerTheme
import org.koin.core.component.inject

internal class TwoC2PPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodRemoteConfig,
) : AsyncPaymentMethodDescriptor(localConfig, options, config), DIAppComponent {

    private val theme: PrimerTheme by inject()

    override val title = "TWOC2P"

    override val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(AsyncPaymentMethodBehaviour(this))

    override fun createButton(container: ViewGroup): View {
        val binding = PaymentMethodButtonTwoc2pBinding.inflate(
            LayoutInflater.from(container.context),
            container,
            false
        )
        val text = binding.twoc2pButtonText
        val drawable = ContextCompat.getDrawable(
            container.context,
            R.drawable.button_2c2p_logo
        )

        text.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

        text.setTextColor(
            theme.paymentMethodButton.text.defaultColor.getColor(
                container.context,
                theme.isDarkMode
            )
        )

        val icon = text.compoundDrawables

        DrawableCompat.setTint(
            DrawableCompat.wrap(icon[0]),
            theme.paymentMethodButton.text.defaultColor.getColor(
                container.context,
                theme.isDarkMode
            )
        )

        return binding.root
    }
}
