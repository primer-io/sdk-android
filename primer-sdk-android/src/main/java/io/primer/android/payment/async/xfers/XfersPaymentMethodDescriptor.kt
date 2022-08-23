package io.primer.android.payment.async.xfers

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

internal class XfersPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "XFERS"

    override fun getLoadingState() = LoadingState(R.drawable.ic_logo_xfers_square)

    override val behaviours: List<SelectedPaymentMethodBehaviour>
        get() = listOf(
            NewFragmentBehaviour(PaymentMethodLoadingFragment::newInstance, true),
        )

    override val sdkCapabilities: List<SDKCapability>
        get() = listOf(SDKCapability.DROP_IN)
}
