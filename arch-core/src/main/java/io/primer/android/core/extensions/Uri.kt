package io.primer.android.core.extensions

import android.net.Uri
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Uri.buildWithQueryParams(params: Map<String, Any>) =
    this.buildUpon()
        .apply {
            params.forEach { entry -> appendQueryParameter(entry.key, entry.value.toString()) }
        }.build().toString()
