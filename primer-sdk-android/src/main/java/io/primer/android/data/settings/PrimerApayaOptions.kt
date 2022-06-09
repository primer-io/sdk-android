package io.primer.android.data.settings

import kotlinx.serialization.Serializable

@Serializable
@Deprecated("This class is deprecated and will be removed in future release.")
data class PrimerApayaOptions(
    var webViewTitle: String? = null,
)
