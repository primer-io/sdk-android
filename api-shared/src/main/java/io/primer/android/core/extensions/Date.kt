package io.primer.android.core.extensions

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

private val iso8601DateFormat: DateFormat by lazy {
    // noinspection SimpleDateFormat
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
}

fun Date.toIso8601String(): String = iso8601DateFormat.format(this)
