package io.primer.android.data.base.exceptions

internal class IllegalValueException(val key: IllegalValueKey, override val message: String?) :
    IllegalArgumentException(message)

internal interface IllegalValueKey {
    val key: String
}
