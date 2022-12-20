package io.primer.android.payment.async.ipay88

import com.ipay.IPayIH
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment

internal class IPay88CardPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    internal val paymentId = "2"

    internal val paymentMethod = IPayIH.PAY_METHOD_CREDIT_CARD

    override val title: String = config.name.orEmpty()

    override val behaviours: List<SelectedPaymentMethodBehaviour>
        get() = listOf(
            NewFragmentBehaviour(PaymentMethodLoadingFragment::newInstance, false),
        )

    companion object {

        const val IPAY88_METHOD_REQUEST_CODE = 1003
    }
}
