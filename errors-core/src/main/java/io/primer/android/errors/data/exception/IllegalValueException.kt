package io.primer.android.errors.data.exception

data class IllegalValueException(val key: IllegalValueKey, override val message: String?) :
    IllegalArgumentException(message)

interface IllegalValueKey {
    val key: String
}
