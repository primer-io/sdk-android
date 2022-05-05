package io.primer.android.payment.gocardless

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PrimerSettings
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.Success

internal class GoCardlessFactory(val settings: PrimerSettings) : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        if (settings.business.name == null) {
            return Failure(Exception("Business name is null"))
        }

        if (settings.business.address == null) {
            return Failure(Exception("Business address is null"))
        }

        val companyName = settings.business.name!!
        val companyAddress = settings.business.address!!.toAddressLine()

        val goCardless = GoCardless(
            companyName,
            companyAddress,
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
