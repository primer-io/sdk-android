package io.primer.android.errors.utils

import io.primer.android.errors.data.exception.IllegalValueException
import io.primer.android.errors.data.exception.IllegalValueKey

fun <T : Any> requireNotNullCheck(value: T?, key: IllegalValueKey): T {
    return try {
        requireNotNull(value) { "Required value for ${key.key} was null." }
    } catch (expected: IllegalArgumentException) {
        throw IllegalValueException(key, expected.message)
    }
}
