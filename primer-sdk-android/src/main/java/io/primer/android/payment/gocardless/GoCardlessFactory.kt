package io.primer.android.payment.gocardless

import io.primer.android.PaymentMethod
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.Success

internal class GoCardlessFactory(val settings: PrimerSettings) : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {

        val goCardlessPaymentOptions = settings.paymentMethodOptions.goCardlessOptions

        if (goCardlessPaymentOptions.businessName == null) {
            return Failure(Exception("Business name is null"))
        }

        if (goCardlessPaymentOptions.businessAddress == null) {
            return Failure(Exception("Business address is null"))
        }
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

        return Success(goCardless)
    }
}
