package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.otp.blik

import io.primer.android.components.domain.core.models.otp.PrimerOtpCodeData
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidator

internal class BlikInputDataValidator : PaymentInputDataValidator<PrimerOtpCodeData> {
    override suspend fun validate(rawData: PrimerOtpCodeData) =
        listOfNotNull(BlikOtpCodeValidator().validate(rawData.otpCode))
}
