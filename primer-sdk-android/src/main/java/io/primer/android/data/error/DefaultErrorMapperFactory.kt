package io.primer.android.data.error

import io.primer.android.components.data.error.HUCErrorMapper
import io.primer.android.data.action.error.ActionUpdateErrorMapper
import io.primer.android.data.payments.apaya.error.SessionCreateErrorMapper
import io.primer.android.data.payments.create.error.PaymentCreateErrorMapper
import io.primer.android.data.payments.iPay88.error.IPayErrorMapper
import io.primer.android.data.payments.klarna.error.KlarnaErrorMapper
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
            ErrorMapperType.I_PAY88 -> IPayErrorMapper()
            ErrorMapperType.DEFAULT -> DefaultErrorMapper()
        }
    }
}
