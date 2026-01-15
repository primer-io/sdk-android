package io.primer.android.threeds.extensions

import java.util.Locale

private const val JOIN_SEPARATOR = "_"
private const val LANGUAGE_SEPARATOR = "-"

internal fun Locale.toNormalizedLocale(): String =
    listOfNotNull(
        language.split(LANGUAGE_SEPARATOR, JOIN_SEPARATOR)
            .firstOrNull()?.lowercase().takeIf { it.isNullOrBlank().not() },
        country.uppercase().takeIf { it.isNotBlank() },
    ).joinToString(JOIN_SEPARATOR)
