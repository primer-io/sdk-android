package io.primer.android.threeds.data.exception

import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams
import io.primer.android.configuration.data.model.CardNetwork

internal class ThreeDsMissingDirectoryServerException(
    val cardNetwork: CardNetwork.Type,
    val context: ThreeDsFailureContextParams,
) : IllegalStateException()
