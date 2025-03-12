package io.primer.android.threeds.extensions

import java.util.Locale

private const val SEPARATOR = "_"

internal fun Locale.toNormalizedLocale() =
    listOf(this.language, this.country).filter { it.isNotBlank() }.joinToString(SEPARATOR)
