package io.primer.android.payment.async.fastbanktransfer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodRemoteConfig
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.databinding.PaymentMethodButtonFastBinding
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.payment.LoadingState

internal class RapydFastPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodRemoteConfig,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "FAST"

    override val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(NewFragmentBehaviour(PaymentMethodLoadingFragment::newInstance, true))

    override fun getLoadingState() = LoadingState(
        if (localConfig.settings.uiOptions.theme.isDarkMode == true) R.drawable.ic_logo_fast_dark
        else R.drawable.ic_logo_fast_light
    )

    override fun createButton(container: ViewGroup): View {
        val binding = PaymentMethodButtonFastBinding.inflate(
            LayoutInflater.from(container.context),
            container,
            false
        )

        val icon = binding.icon
        icon.setImageResource(
            if (localConfig.settings.uiOptions.theme.isDarkMode == true)
                R.drawable.ic_logo_fast_dark
            else R.drawable.ic_logo_fast_light
        )
        return binding.root
    }
}
