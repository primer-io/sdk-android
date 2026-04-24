package io.primer.checkout.orchestrator

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object ManifestOverrides {
    var url: String? = null
        set(value) {
            field = if (BuildConfig.DEBUG) value else null
        }
}
