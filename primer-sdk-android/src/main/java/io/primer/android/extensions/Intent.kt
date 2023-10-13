package io.primer.android.extensions

import android.content.Intent
import android.os.Build

internal inline fun <reified T> Intent.getParcelable(name: String) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        this.getParcelableExtra(name)
    }
