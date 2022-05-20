package io.primer.android.payment.async.coinbase

import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.databinding.PaymentMethodButtonCoinbaseBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.payment.utils.ButtonViewHelper.generateButtonContent
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class CoinbasePaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodRemoteConfig,
) : AsyncPaymentMethodDescriptor(localConfig, options, config), DIAppComponent {

    private val theme: PrimerTheme by inject()

    override val title = "COINBASE"

    override val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(AsyncPaymentMethodBehaviour(this))

    override fun createButton(container: ViewGroup): View {
        val binding = PaymentMethodButtonCoinbaseBinding.inflate(
            LayoutInflater.from(container.context),
            container,
            false
        )

        val content = generateButtonContent(theme, container.context)
        val splash = theme.splashColor.getColor(container.context, theme.isDarkMode)
        val rippleColor = ColorStateList.valueOf(splash)
        binding.cardPreviewButton.background = RippleDrawable(rippleColor, content, null)

        val text = binding.cardPreviewButtonText
        val drawable = ContextCompat.getDrawable(
            container.context,
            R.drawable.ic_coinbase_wallet
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
