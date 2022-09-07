package io.primer.android.core.serialization.json.extensions

import org.json.JSONArray

internal fun <T> JSONArray.sequence(): Sequence<T> =
    (0 until this.length()).asSequence().map { this.get(it) as T }
