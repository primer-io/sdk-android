package io.primer.android.components.domain.payments.metadata.empty

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.payments.metadata.PaymentRawDataMetadataRetriever

internal class EmptyMetadataRetriever : PaymentRawDataMetadataRetriever<PrimerRawData> {
    override suspend fun retrieveMetadata(inputData: PrimerRawData) = null
}
