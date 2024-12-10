package io.primer.android.paymentmethods.manager.composable

import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import kotlinx.coroutines.flow.Flow

interface PrimerHeadlessMetadatable<T : PrimerCollectableData> {

    val metadataFlow: Flow<PrimerPaymentMethodMetadata>
}

interface PrimerHeadlessStateMetadatable<T : PrimerCollectableData> {

    val metadataStateFlow: Flow<PrimerPaymentMethodMetadataState>
}
