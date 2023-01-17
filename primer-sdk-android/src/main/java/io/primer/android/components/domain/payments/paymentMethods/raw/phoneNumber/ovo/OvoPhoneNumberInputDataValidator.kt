package io.primer.android.components.domain.payments.paymentMethods.raw.phoneNumber.ovo

import io.primer.android.components.domain.core.models.phoneNumber.PrimerPhoneNumberData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class OvoPhoneNumberInputDataValidator :
    PaymentInputDataValidator<PrimerPhoneNumberData> {
    override fun validate(rawData: PrimerPhoneNumberData):
        Flow<List<PrimerInputValidationError>> {
        return flow {
            emit(listOfNotNull(OvoPhoneNumberValidator().validate(rawData.phoneNumber)))
        }
    }
}
