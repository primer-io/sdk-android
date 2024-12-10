package io.primer.android.core.logging.extensions

internal fun Pair<String, String>.appendHeader(
    stringBuilder: StringBuilder,
    excludedHeaders: Set<String>,
    shouldObfuscate: Boolean,
    blacklistedHeaders: List<String>,
    obfuscationString: String
) {
    if (excludedHeaders.any { first.equals(it, ignoreCase = true) }) {
        return
    }
    val name = first
    val value = if (shouldObfuscate && blacklistedHeaders.any { it.equals(name, true) }) {
        obfuscationString
    } else {
        second
    }
    stringBuilder.append("\n$name: $value")
}
