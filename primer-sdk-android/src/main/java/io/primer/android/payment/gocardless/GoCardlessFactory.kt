package io.primer.android.payment.gocardless

import io.primer.android.PaymentMethod
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.Success

internal class GoCardlessFactory(val settings: PrimerSettings) : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {

        val goCardlessPaymentOptions = settings.paymentMethodOptions.goCardlessOptions

        return if (goCardlessPaymentOptions.businessName == null) {
            Failure(Exception("Business name is null"))
        } else if (goCardlessPaymentOptions.businessAddress == null) {
            Failure(Exception("Business address is null"))
        } else {
            val goCardless = GoCardless(
                goCardlessPaymentOptions.businessName,
                goCardlessPaymentOptions.businessAddress,
                "${settings.customer.firstName.orEmpty()} ${settings.customer.lastName.orEmpty()}",
                settings.customer.emailAddress,
                settings.customer.billingAddress?.addressLine1,
                settings.customer.billingAddress?.addressLine2,
                settings.customer.billingAddress?.city,
                null,
                settings.customer.billingAddress?.countryCode?.name,
                settings.customer.billingAddress?.postalCode,
            )
            Success(goCardless)
        }
    }
}
