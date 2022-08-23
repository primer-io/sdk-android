package io.primer.android.components.domain.core.models.phoneNumber

import io.primer.android.components.domain.core.models.PrimerAsyncRawDataTokenizationHelper
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.payment.async.ovo.XenditOvoPaymentMethodDescriptor

data class PrimerRawPhoneNumberData(val phoneNumber: String) : PrimerRawData {

    internal fun setTokenizableValues(
        descriptor: XenditOvoPaymentMethodDescriptor,
        redirectionUrl: String
    ) =
        PrimerAsyncRawDataTokenizationHelper(redirectionUrl).setTokenizableData(descriptor).apply {
            appendTokenizableValue(
                "sessionInfo",
                PrimerInputElementType.PHONE_NUMBER.field,
                phoneNumber
            )
        }
}
