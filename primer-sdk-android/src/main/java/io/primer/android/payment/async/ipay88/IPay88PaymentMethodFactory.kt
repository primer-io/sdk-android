package io.primer.android.payment.async.ipay88

import io.primer.android.PaymentMethod
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.ipay88.helpers.IPay88SdkClassValidator
import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.Success

internal class IPay88PaymentMethodFactory(private val type: String, val settings: PrimerSettings) :
    PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        val iPay88 = AsyncPaymentMethod(
            type
        )

        if (IPay88SdkClassValidator().isIPaySdkIncluded().not()) {
            return Failure(
                IllegalStateException(
                    IPay88SdkClassValidator.I_PAY_CLASS_NOT_LOADED_ERROR
                        .format(
                            type,
                            settings.order.countryCode?.name?.lowercase(),
                            settings.order.countryCode?.name?.lowercase(),
                            type
                        )
                )
            )
        }
        return Success(iPay88)
    }
}
