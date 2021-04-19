package io.primer.android.payment.google

import io.primer.android.payment.GOOGLE_PAY_IDENTIFIER
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.viewmodel.GooglePayPaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodCheckerRegistrar
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension

internal abstract class _InitialCheckRequiredBehaviour(

) : SelectedPaymentMethodBehaviour() {

    abstract fun initialize(
        paymentMethodCheckerRegistrar: PaymentMethodCheckerRegistrar,
        viewModel: TokenizationViewModel,
    )

    fun execute(
        paymentMethodCheckerRegistrar: PaymentMethodCheckerRegistrar,
        viewModel: TokenizationViewModel,
    ) {
        initialize(paymentMethodCheckerRegistrar, viewModel)
    }
}

// 1. PaymentsUtil.createPaymentsClient(this)
// 2. possiblyShowGooglePayButton()
// 3. requestPayment()
// 4. onActivityResult()
// 5. handlePaymentSuccess()

@KoinApiExtension
internal class GooglePayBehaviour constructor(
    private val paymentMethodDescriptor: GooglePayDescriptor,
    private val googlePayPaymentMethodChecker: PaymentMethodChecker
) : _InitialCheckRequiredBehaviour() {

    override fun initialize(
        paymentMethodCheckerRegistrar: PaymentMethodCheckerRegistrar,
        viewModel: TokenizationViewModel,
    ) {
        paymentMethodCheckerRegistrar.register(
            GOOGLE_PAY_IDENTIFIER,
            googlePayPaymentMethodChecker
        )

        viewModel.resetPaymentMethod(paymentMethodDescriptor)
    }
}
