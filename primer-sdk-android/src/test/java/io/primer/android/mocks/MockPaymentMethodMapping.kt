package io.primer.android.mocks

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.models.PaymentMethodImplementationType
import io.primer.android.data.payments.methods.mapping.PaymentMethodMapping
import io.primer.android.payment.card.CardFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Failure

internal class MockPaymentMethodMapping(
    private val throwError: Boolean = false
) : PaymentMethodMapping {

    var getPaymentMethodForCalled: Boolean = false

    override fun getPaymentMethodFor(
        implementationType: PaymentMethodImplementationType,
        type: String
    ): Either<PaymentMethod, Exception> {
        getPaymentMethodForCalled = true
        return if (throwError) {
            Failure(Exception("error!"))
        } else {
            CardFactory().build()
        }
    }
}
