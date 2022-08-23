package io.primer.android.components.domain.payments.validation.otp.blik

import io.primer.android.components.domain.core.models.otp.PrimerOtpCodeRawData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.payments.validation.PaymentInputDataValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class BlikInputDataValidator : PaymentInputDataValidator<PrimerOtpCodeRawData> {
    override fun validate(rawData: PrimerOtpCodeRawData): Flow<List<PrimerInputValidationError>?> {
        return flow {
            emit(listOfNotNull(BlikOtpCodeValidator().validate(rawData.otpCode)))
        }
    }
}
