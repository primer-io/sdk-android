package io.primer.android.threeds.data.exception

import io.primer.android.configuration.data.model.CardNetwork

internal class ThreeDsMissingDirectoryServerException(
    val cardNetwork: CardNetwork.Type,
    val threeDsSdkVersion: String?,
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
) : IllegalStateException()
