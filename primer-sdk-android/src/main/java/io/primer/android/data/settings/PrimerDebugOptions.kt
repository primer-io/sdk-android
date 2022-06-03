package io.primer.android.data.settings

import kotlinx.serialization.Serializable

@Serializable
data class PrimerDebugOptions(val is3DSSanityCheckEnabled: Boolean = true)
