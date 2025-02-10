package io.primer.android.errors.data.exception

class IllegalClientSessionValueException(
    val key: IllegalValueKey,
    val value: Any?,
    val allowedValue: Any? = null,
) : IllegalArgumentException()
