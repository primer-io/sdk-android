package io.primer.android.components.domain.payments.metadata

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata

internal fun interface PaymentRawDataMetadataRetriever<out T : PrimerRawData> {

    suspend fun retrieveMetadata(inputData: @UnsafeVariance T): PrimerPaymentMethodMetadata?
}
