package io.primer.android.payment.google

import io.primer.android.payment.GOOGLE_PAY_IDENTIFIER
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.viewmodel.PaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodCheckerRegistrar
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension

internal abstract class InitialCheckRequiredBehaviour : SelectedPaymentMethodBehaviour() {

    abstract fun initialize(
        paymentMethodCheckerRegistrar: PaymentMethodCheckerRegistrar,
        viewModel: TokenizationViewModel,
    )

    abstract fun execute(
        viewModel: TokenizationViewModel,
    )
}

// 1. PaymentsUtil.createPaymentsClient(this)   done
// 2. possiblyShowGooglePayButton()             done
// 3. requestPayment()                          TODO
// 4. onActivityResult()                        TODO
// 5. handlePaymentSuccess()                    TODO

@KoinApiExtension
internal class GooglePayBehaviour constructor(
    private val paymentMethodDescriptor: GooglePayDescriptor,
    private val googlePayPaymentMethodChecker: PaymentMethodChecker,
) : InitialCheckRequiredBehaviour() {

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

    override fun execute(viewModel: TokenizationViewModel) {
        // TODO: execute not implemented
    }
}
