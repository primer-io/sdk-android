package io.primer.android.components.domain.payments.metadata

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadata
import kotlinx.coroutines.flow.Flow

internal interface PaymentRawDataMetadataRetriever<in T : PrimerRawData> {

    fun retrieveMetadata(inputData: T): Flow<PrimerCardMetadata>
}
