package io.primer.android.configuration.extension

fun String.sanitizedCardNumber() = this.replace(Regex("[^0-9]"), "")
