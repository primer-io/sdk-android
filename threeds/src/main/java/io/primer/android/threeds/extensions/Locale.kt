package io.primer.android.threeds.extensions

import java.util.Locale

private const val JOIN_SEPARATOR = "_"
private const val LANGUAGE_SEPARATOR = "-"

internal fun Locale.toNormalizedLocale() =
    listOf(this.language.split(LANGUAGE_SEPARATOR).firstOrNull(), this.country).filter { it.orEmpty().isNotBlank() }
        .joinToString(JOIN_SEPARATOR)
