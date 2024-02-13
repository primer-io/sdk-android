package io.primer.android.utils

internal fun String.removeSpaces() = this.replace("\\s".toRegex(), "")

internal fun String.sanitized() = this.trim()

internal fun String?.orNull() = if (isNullOrBlank()) null else this

internal fun String.sanitizedCardNumber() = this.replace(Regex("[^0-9]"), "")
