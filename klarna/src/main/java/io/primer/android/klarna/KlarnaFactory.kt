package io.primer.android.klarna

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Failure
import io.primer.android.core.utils.Success
import io.primer.android.klarna.implementation.helpers.KlarnaSdkClassValidator
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory

class KlarnaFactory(private val type: String) : PaymentMethodFactory {
    override fun build(): Either<PaymentMethod, Exception> {
        val klarna = Klarna(type)

        if (KlarnaSdkClassValidator().isKlarnaSdkIncluded().not()) {
            return Failure(
                IllegalStateException(
                    KlarnaSdkClassValidator.KLARNA_CLASS_NOT_LOADED_ERROR,
                ),
            )
        }

        return Success(klarna)
    }
}
