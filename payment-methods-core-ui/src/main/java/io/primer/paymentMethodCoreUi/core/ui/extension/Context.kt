package io.primer.paymentMethodCoreUi.core.ui.extension

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.RestrictTo
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal fun Context.withLocale(locale: Locale): Context {
    val config = Configuration(resources.configuration)
    Locale.setDefault(locale)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}
