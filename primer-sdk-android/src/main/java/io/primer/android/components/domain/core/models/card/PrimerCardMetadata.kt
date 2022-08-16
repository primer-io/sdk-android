package io.primer.android.components.domain.core.models.card

import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.ui.CardNetwork

data class PrimerCardMetadata(val cardNetwork: CardNetwork.Type) :
    PrimerPaymentMethodMetadata
