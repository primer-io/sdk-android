package io.primer.android.components.domain.core.models.bancontact

import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.ui.CardNetwork

data class PrimerBancontactCardMetadata(val cardNetwork: CardNetwork.Type) :
    PrimerPaymentMethodMetadata
