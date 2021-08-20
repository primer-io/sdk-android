package io.primer.android.model

import kotlinx.serialization.Serializable

@Serializable
data class PrimerDebugOptions(val is3DSSanityCheckEnabled: Boolean = true)
