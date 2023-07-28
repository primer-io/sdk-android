package io.primer.android.payment.async.ipay88

import com.ipay.IPayIH
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.SelectedPaymentMethodManagerBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import org.koin.core.component.get

internal class IPay88PaymentMethodDescriptor(
    override val options: AsyncPaymentMethod,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(options, localConfig, config) {

    internal val paymentMethod = IPayIH.PAY_METHOD_CREDIT_CARD

    override val title: String = config.name.orEmpty()

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI))

    override val resumeHandler: PrimerResumeDecisionHandler
        get() = get()

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = SelectedPaymentMethodManagerBehaviour(options.type, localConfig.paymentMethodIntent)
}
