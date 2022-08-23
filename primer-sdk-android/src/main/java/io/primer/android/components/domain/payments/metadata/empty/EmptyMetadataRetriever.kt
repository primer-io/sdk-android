package io.primer.android.components.domain.payments.metadata.empty

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.payments.metadata.PaymentRawDataMetadataRetriever
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class EmptyMetadataRetriever : PaymentRawDataMetadataRetriever<PrimerRawData> {
    override fun retrieveMetadata(inputData: PrimerRawData): Flow<PrimerPaymentMethodMetadata?> {
        return flow { emit(null) }
    }
}
