package io.primer.android.core.extensions

import okhttp3.MediaType
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

internal val MediaType?.charset: Charset
    get() = this?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
