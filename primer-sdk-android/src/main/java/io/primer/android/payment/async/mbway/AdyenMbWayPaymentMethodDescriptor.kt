package io.primer.android.payment.async.mbway

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodRemoteConfig
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.fragments.forms.DynamicFormFragment
import io.primer.android.ui.payment.LoadingState

internal class AdyenMbWayPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodRemoteConfig,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "MB WAY"

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override fun getLoadingState() =
        LoadingState(
            when (localConfig.settings.uiOptions.theme.isDarkMode) {
                true -> R.drawable.ic_logo_mbway_light
                else -> R.drawable.ic_logo_mbway
            },
            R.string.payment_method_mb_way_loading_placeholder
        )

    override val selectedBehaviour =
        NewFragmentBehaviour(
            DynamicFormFragment::newInstance, returnToPreviousOnBack = true
        )

    override val behaviours: List<SelectedPaymentMethodBehaviour>
        get() = listOf(
            AsyncPaymentMethodBehaviour(this),
            NewFragmentBehaviour(PaymentMethodLoadingFragment::newInstance)
        )

    override fun createButton(container: ViewGroup): View {
        return LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_mbway,
            container,
            false
        )
    }
}
