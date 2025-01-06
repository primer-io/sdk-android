package io.primer.android.bancontact.implementation.metadata.domain.model

import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.configuration.data.model.CardNetwork

data class PrimerBancontactCardMetadata(val cardNetwork: CardNetwork.Type) : PrimerPaymentMethodMetadata
