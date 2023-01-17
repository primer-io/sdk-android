package io.primer.android.components.domain.payments.paymentMethods.raw.otp.blik

import io.primer.android.components.domain.core.models.otp.PrimerOtpCodeData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class BlikInputDataValidator : PaymentInputDataValidator<PrimerOtpCodeData> {
    override fun validate(rawData: PrimerOtpCodeData): Flow<List<PrimerInputValidationError>> {
        return flow {
            emit(listOfNotNull(BlikOtpCodeValidator().validate(rawData.otpCode)))
        }
    }
}
