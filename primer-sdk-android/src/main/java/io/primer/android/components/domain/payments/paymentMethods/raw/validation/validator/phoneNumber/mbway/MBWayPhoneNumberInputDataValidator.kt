package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.phoneNumber.mbway

import io.primer.android.components.domain.core.models.phoneNumber.PrimerPhoneNumberData
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidator

internal class MBWayPhoneNumberInputDataValidator :
    PaymentInputDataValidator<PrimerPhoneNumberData> {
    override suspend fun validate(rawData: PrimerPhoneNumberData) =
        listOfNotNull(MBWayPhoneNumberValidator().validate(rawData.phoneNumber))
}
