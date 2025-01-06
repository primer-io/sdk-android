package io.primer.android.errors.data.exception

class IllegalClientSessionValueException(
    val key: io.primer.android.errors.data.exception.IllegalValueKey,
    val value: Any?,
    val allowedValue: Any? = null,
) : IllegalArgumentException()
