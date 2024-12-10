package io.primer.cardShared

import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import io.primer.android.paymentmethods.PrimerRawData
import kotlinx.coroutines.flow.Flow

interface PaymentRawDataMetadataStateRetriever<out T : PrimerRawData,
    out R : PrimerPaymentMethodMetadataState> {

    val metadataState: Flow<R>

    suspend fun handleInputData(inputData: @UnsafeVariance T)
}
