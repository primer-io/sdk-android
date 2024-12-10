package io.primer.android.utils

internal fun String.removeSpaces() = this.replace("\\s".toRegex(), "")

internal fun String?.orNull() = if (isNullOrBlank()) null else this
