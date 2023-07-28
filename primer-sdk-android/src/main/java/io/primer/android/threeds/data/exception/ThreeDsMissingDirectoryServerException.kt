package io.primer.android.threeds.data.exception

import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams
import io.primer.android.threeds.data.models.common.CardNetwork

internal class ThreeDsMissingDirectoryServerException(
    val cardNetwork: CardNetwork,
    val context: ThreeDsFailureContextParams
) : IllegalStateException()
