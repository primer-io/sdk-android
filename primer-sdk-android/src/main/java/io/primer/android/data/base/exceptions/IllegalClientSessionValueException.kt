package io.primer.android.data.base.exceptions

internal class IllegalClientSessionValueException(
    val key: IllegalValueKey,
    val value: Any?,
    val allowedValue: Any? = null
) : IllegalArgumentException()
