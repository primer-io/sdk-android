package io.primer.android.payment.apaya

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PrimerSettings
import io.primer.android.payment.PaymentMethodFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Success

internal class ApayaFactory(private val settings: PrimerSettings) : PaymentMethodFactory() {

    override fun build(): Either<PaymentMethod, Exception> {
        return Success(Apaya(settings.options.apayaWebViewTitle ?: "Pay by mobile"))
    }
}
