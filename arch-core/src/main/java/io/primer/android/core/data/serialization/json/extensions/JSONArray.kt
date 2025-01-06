package io.primer.android.core.data.serialization.json.extensions

import org.json.JSONArray

fun <T> JSONArray.sequence(): Sequence<T> = (0 until this.length()).asSequence().map { this[it] as T }
