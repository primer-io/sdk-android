@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.banks.descriptor

import androidx.annotation.VisibleForTesting
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.LoadingState
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.fragments.bank.DotPayBankSelectionFragment
import io.primer.android.ui.fragments.bank.IdealBankSelectionFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal data class BankIssuerDropInDescriptor(
    override val paymentMethodType: String,
    override val uiOptions: UiOptions,
) : PaymentMethodDropInDescriptor {
    @VisibleForTesting
    val fragmentFactory =
        when (paymentMethodType) {
            PaymentMethodType.ADYEN_IDEAL.name -> IdealBankSelectionFragment::newInstance
            PaymentMethodType.ADYEN_DOTPAY.name -> DotPayBankSelectionFragment::newInstance
            else -> error("Unsupported payment method type '$paymentMethodType'")
        }

    override val selectedBehaviour: PaymentMethodBehaviour
        get() =
            NewFragmentBehaviour(
                factory = fragmentFactory,
                returnToPreviousOnBack = uiOptions.isStandalonePaymentMethod.not(),
            )

    override val uiType: PaymentMethodUiType
        get() = PaymentMethodUiType.FORM

    override val behaviours: List<PaymentMethodBehaviour>
        get() = emptyList()

    override val loadingState: LoadingState?
        get() = null
}
