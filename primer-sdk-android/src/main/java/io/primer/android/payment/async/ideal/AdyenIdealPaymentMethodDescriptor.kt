package io.primer.android.payment.async.ideal

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
import io.primer.android.ui.fragments.bank.IdealBankSelectionFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
@ExperimentalCoroutinesApi
internal class AdyenIdealPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodRemoteConfig,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "IDEAL"

    override val selectedBehaviour =
        NewFragmentBehaviour(IdealBankSelectionFragment::newInstance, returnToPreviousOnBack = true)

    override val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(AsyncPaymentMethodBehaviour(this))

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override fun createButton(container: ViewGroup): View {
        return LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_ideal,
            container,
            false
        )
    }
}
