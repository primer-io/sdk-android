package io.primer.android.utils

import android.net.Uri

internal fun Uri.buildWithQueryParams(params: Map<String, Any>) = this.buildUpon()
    .apply {
        params.forEach { entry -> appendQueryParameter(entry.key, entry.value.toString()) }
    }.build().toString()
