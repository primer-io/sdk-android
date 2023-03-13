package io.primer.android.data.error

import io.primer.android.components.data.error.HUCErrorMapper
import io.primer.android.data.action.error.ActionUpdateErrorMapper
import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.error.ApayaErrorMapper
import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.error.SessionCreateErrorMapper
import io.primer.android.data.payments.create.error.PaymentCreateErrorMapper
import io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.error.GooglePayErrorMapper
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.error.KlarnaErrorMapper
import io.primer.android.components.data.payments.paymentMethods.nativeUi.iPay88.error.IPayErrorMapper
import io.primer.android.data.payments.methods.error.PaymentMethodsErrorMapper
import io.primer.android.data.payments.resume.error.PaymentResumeErrorMapper
import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.error.ErrorMapperFactory
import io.primer.android.domain.error.ErrorMapperType

internal class DefaultErrorMapperFactory : ErrorMapperFactory {

    override fun buildErrorMapper(type: ErrorMapperType): ErrorMapper {
        return when (type) {
            ErrorMapperType.ACTION_UPDATE -> ActionUpdateErrorMapper()
            ErrorMapperType.PAYMENT_CREATE -> PaymentCreateErrorMapper()
            ErrorMapperType.PAYMENT_RESUME -> PaymentResumeErrorMapper()
            ErrorMapperType.SESSION_CREATE -> SessionCreateErrorMapper()
            ErrorMapperType.HUC -> HUCErrorMapper()
            ErrorMapperType.PAYMENT_METHODS -> PaymentMethodsErrorMapper()
            ErrorMapperType.KLARNA -> KlarnaErrorMapper()
            ErrorMapperType.GOOGLE_PAY -> GooglePayErrorMapper()
            ErrorMapperType.APAYA -> ApayaErrorMapper()
            ErrorMapperType.I_PAY88 -> IPayErrorMapper()
            ErrorMapperType.DEFAULT -> DefaultErrorMapper()
        }
    }
}
