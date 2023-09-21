package io.primer.android.payment.nolpay

import com.snowballtech.transit.rta.TransitNfcStatus
import io.primer.android.PaymentMethod
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.payment.nolpay.helpers.NolPaySdkClassValidator
import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.Success
import io.primer.nolpay.api.PrimerNolPayNfcUtils

internal class NolPayFactory : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        return when {
            NolPaySdkClassValidator().isSdkIncluded().not() ->
                Failure(
                    IllegalStateException(
                        NolPaySdkClassValidator.NOL_PAY_CLASS_NOT_LOADED_ERROR
                    )
                )

            PrimerNolPayNfcUtils.getNfcStatus() == TransitNfcStatus.NFC_UNSUPPORTED ->
                Failure(
                    IllegalStateException("NFC is not supported on the current device.")
                )
            else -> Success(NolPay())
        }
    }
}
