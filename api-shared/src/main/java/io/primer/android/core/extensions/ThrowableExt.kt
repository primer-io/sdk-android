package io.primer.android.core.extensions

fun Throwable.requireCause() = requireNotNull(cause)
