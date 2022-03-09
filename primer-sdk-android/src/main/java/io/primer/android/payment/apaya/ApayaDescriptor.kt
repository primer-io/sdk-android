package io.primer.android.payment.apaya

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.databinding.PaymentMethodButtonPayMobileBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.ui.payment.LoadingState
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class ApayaDescriptor constructor(
    val localConfig: PrimerConfig,
    val options: Apaya,
    config: PaymentMethodRemoteConfig,
) : PaymentMethodDescriptor(config), DIAppComponent {

    companion object {

        const val APAYA_REQUEST_CODE = 1001
    }

    private val theme: PrimerTheme by inject()

    override val selectedBehaviour: SelectedPaymentMethodBehaviour = RecurringApayaBehaviour(this)

    override val type: PaymentMethodUiType
        get() = PaymentMethodUiType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.VAULT_ONLY

    override fun createButton(container: ViewGroup): View {
        val binding = PaymentMethodButtonPayMobileBinding.inflate(
            LayoutInflater.from(container.context),
            container,
            false
        )
        val text = binding.payMobilePreviewButton
        val drawable = ContextCompat.getDrawable(
            container.context,
            R.drawable.ic_logo_apaya
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

    override fun getLoadingState() = LoadingState(R.drawable.ic_logo_apaya)
}
