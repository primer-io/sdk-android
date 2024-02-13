package io.primer.android.components.domain.payments.metadata

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import kotlinx.coroutines.flow.Flow

internal interface PaymentRawDataMetadataStateRetriever<out T : PrimerRawData,
    out R : PrimerPaymentMethodMetadataState> {

    val metadataState: Flow<R>

    suspend fun handleInputData(inputData: @UnsafeVariance T)
}
