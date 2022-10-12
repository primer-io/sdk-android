package io.primer.android.components.domain.core.models.retail_outlet

import io.primer.android.components.domain.core.models.PrimerAsyncRawDataTokenizationHelper
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor

data class PrimerRawRetailerData(
    val id: String
) : PrimerRawData {

    internal fun setTokenizableValues(
        descriptor: AsyncPaymentMethodDescriptor,
        redirectionUrl: String
    ) =
        PrimerAsyncRawDataTokenizationHelper(redirectionUrl).setTokenizableData(descriptor).apply {
            appendTokenizableValue(
                "sessionInfo",
                PrimerInputElementType.RETAIL_OUTLET.field,
                id
            )
        }
}
