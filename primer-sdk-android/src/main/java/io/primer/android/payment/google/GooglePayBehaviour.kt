package io.primer.android.payment.google

import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension

internal abstract class _NewBehaviour(
    //
) : SelectedPaymentMethodBehaviour() {

    fun execute(viewModel: TokenizationViewModel) {
        initialize(viewModel)

    }

    abstract fun initialize(viewModel: TokenizationViewModel)
}

// 1. PaymentsUtil.createPaymentsClient(this)
// 2. possiblyShowGooglePayButton()
// 3. requestPayment()
// 4. onActivityResult()
// 5. handlePaymentSuccess()

interface GooglePayBehaviourExecutor {

    fun start()
}

@KoinApiExtension
internal class GooglePayBehaviour constructor(
    private val paymentMethodDescriptor: GooglePay,
) : _NewBehaviour() {

    override fun initialize(viewModel: TokenizationViewModel) {
        viewModel.resetPaymentMethod(paymentMethodDescriptor)
        // TODO issue request to check if google pay should be available
    }
}
