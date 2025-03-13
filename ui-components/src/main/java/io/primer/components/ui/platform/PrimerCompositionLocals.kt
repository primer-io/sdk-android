package io.primer.components.ui.platform

import androidx.compose.runtime.compositionLocalOf
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.DISdkContext
import io.primer.android.data.settings.internal.PrimerConfig

internal val LocalPrimerConfig = compositionLocalOf<PrimerConfig> { resolvePrimerConfig() }

internal val LocalPrimerSessionIntent = compositionLocalOf<PrimerSessionIntent> {
    resolvePrimerConfig().paymentMethodIntent
}

private fun resolvePrimerConfig() = DISdkContext.container().resolve<PrimerConfig>()
