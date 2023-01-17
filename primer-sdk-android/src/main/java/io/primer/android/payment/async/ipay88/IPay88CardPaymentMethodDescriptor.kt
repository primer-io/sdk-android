package io.primer.android.payment.async.ipay88

import com.ipay.IPayIH
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.SelectedPaymentMethodManagerBehaviour
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

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI))

    override val behaviours: List<SelectedPaymentMethodBehaviour>
        get() = listOf(
            NewFragmentBehaviour(PaymentMethodLoadingFragment::newInstance, false),
        )
    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = SelectedPaymentMethodManagerBehaviour(options.type, localConfig.paymentMethodIntent)
}
