package io.primer.android.payment.nolpay

import com.snowballtech.transit.rta.Transit
import com.snowballtech.transit.rta.TransitNfcStatus
import io.primer.android.PaymentMethod
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.Success

internal class NolPayFactory : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        return when (Transit.getTransitInstance().getNfcStatus()) {
            TransitNfcStatus.NFC_UNSUPPORTED -> Failure(
                IllegalStateException("NFC is not supported on the current device.")
            )
            else -> Success(NolPay())
        }
    }
}
