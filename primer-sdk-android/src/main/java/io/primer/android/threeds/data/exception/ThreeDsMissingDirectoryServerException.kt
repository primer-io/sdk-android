package io.primer.android.threeds.data.exception

import io.primer.android.threeds.data.models.CardNetwork

internal class ThreeDsMissingDirectoryServerException(val cardNetwork: CardNetwork) :
    IllegalStateException()
