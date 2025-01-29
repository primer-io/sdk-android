package io.primer.android.threeds.ui.launcher

import java.io.Serializable

@Suppress("SerialVersionUIDInSerializableClass")
data class ThreeDsActivityLauncherParams(val supportedThreeDsProtocolVersions: List<String>) : Serializable
