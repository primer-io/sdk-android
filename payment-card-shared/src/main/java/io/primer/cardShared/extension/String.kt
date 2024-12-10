package io.primer.cardShared.extension

fun String.sanitizedCardNumber() = this.replace(Regex("[^0-9]"), "")

fun String.removeSpaces() = this.replace("\\s".toRegex(), "")
