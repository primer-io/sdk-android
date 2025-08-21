package io.primer.android.threeds.ui.launcher

import io.primer.android.configuration.data.model.CardNetwork
import java.io.Serializable

@Suppress("SerialVersionUIDInSerializableClass")
data class ThreeDsActivityLauncherParams(
    val supportedThreeDsProtocolVersions: List<String>,
    val paymentMethodToken: String,
    val cardNetwork: CardNetwork.Type,
) : Serializable
