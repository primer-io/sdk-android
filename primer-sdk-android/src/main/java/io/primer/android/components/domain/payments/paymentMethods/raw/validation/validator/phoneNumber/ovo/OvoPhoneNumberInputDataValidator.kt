package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.phoneNumber.ovo

import io.primer.android.components.domain.core.models.phoneNumber.PrimerPhoneNumberData
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidator

internal class OvoPhoneNumberInputDataValidator :
    PaymentInputDataValidator<PrimerPhoneNumberData> {
    override suspend fun validate(rawData: PrimerPhoneNumberData) =
        listOfNotNull(OvoPhoneNumberValidator().validate(rawData.phoneNumber))
}
