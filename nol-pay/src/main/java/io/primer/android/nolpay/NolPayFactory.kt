package io.primer.android.nolpay

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Failure
import io.primer.android.core.utils.Success
import io.primer.android.nolpay.implementation.helpers.NolPaySdkClassValidator
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory
import io.primer.nolpay.api.PrimerNolPayNfcUtils
import io.primer.nolpay.api.models.NfcStatus

class NolPayFactory : PaymentMethodFactory {
    override fun build(): Either<PaymentMethod, Exception> {
        return when {
            NolPaySdkClassValidator().isSdkIncluded().not() ->
                Failure(
                    IllegalStateException(
                        NolPaySdkClassValidator.NOL_PAY_CLASS_NOT_LOADED_ERROR,
                    ),
                )

            PrimerNolPayNfcUtils.getNfcStatus() == NfcStatus.NFC_UNSUPPORTED ->
                Failure(
                    IllegalStateException("NFC is not supported on the current device."),
                )

            else -> Success(NolPay())
        }
    }
}
