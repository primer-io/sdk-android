package io.primer.android.components.domain.payments.validation.phoneNumber.ovo

import io.primer.android.components.domain.core.models.phoneNumber.PrimerRawPhoneNumberData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.payments.validation.PaymentInputDataValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class OvoPhoneNumberInputDataValidator :
    PaymentInputDataValidator<PrimerRawPhoneNumberData> {
    override fun validate(rawData: PrimerRawPhoneNumberData):
        Flow<List<PrimerInputValidationError>?> {
        return flow {
            emit(listOfNotNull(OvoPhoneNumberValidator().validate(rawData.phoneNumber)))
        }
    }
}
