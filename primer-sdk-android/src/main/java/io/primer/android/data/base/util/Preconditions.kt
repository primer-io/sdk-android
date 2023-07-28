package io.primer.android.data.base.util

import io.primer.android.data.base.exceptions.IllegalValueException
import io.primer.android.data.base.exceptions.IllegalValueKey

internal fun <T : Any> requireNotNullCheck(value: T?, key: IllegalValueKey): T {
    return try {
        requireNotNull(value) { "Required value for $key was null." }
    } catch (expected: IllegalArgumentException) {
        throw IllegalValueException(key, expected.message)
    }
}
