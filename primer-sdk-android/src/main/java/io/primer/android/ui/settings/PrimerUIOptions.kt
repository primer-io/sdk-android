package io.primer.android.ui.settings

import kotlinx.serialization.Serializable

@Serializable
data class PrimerUIOptions(
    var isInitScreenEnabled: Boolean = true,
    var isSuccessScreenEnabled: Boolean = true,
    var isErrorScreenEnabled: Boolean = true,
    var theme: PrimerTheme = PrimerTheme.build()
)
