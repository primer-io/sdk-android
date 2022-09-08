package io.primer.android.ui.settings

data class PrimerUIOptions(
    var isInitScreenEnabled: Boolean = true,
    var isSuccessScreenEnabled: Boolean = true,
    var isErrorScreenEnabled: Boolean = true,
    var theme: PrimerTheme = PrimerTheme.build()
)
