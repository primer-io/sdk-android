package io.primer.android.payment.async.fastbanktransfer

import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.SDKCapability
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.payment.LoadingState

internal class RapydFastPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "FAST"

    override val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(NewFragmentBehaviour(PaymentMethodLoadingFragment::newInstance, true))

    override val sdkCapabilities: List<SDKCapability>
        get() = listOf(SDKCapability.DROP_IN)

    override fun getLoadingState() = LoadingState(
        if (localConfig.settings.uiOptions.theme.isDarkMode == true) R.drawable.ic_logo_fast_dark
        else R.drawable.ic_logo_fast_light
    )
}
