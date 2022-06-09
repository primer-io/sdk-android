package io.primer.android.data.settings

import kotlinx.serialization.Serializable

@Serializable
@Deprecated("This class is deprecated and will be removed in future release.")
data class PrimerGoCardlessOptions(
    var businessName: String? = null,
    var businessAddress: String? = null,
)
