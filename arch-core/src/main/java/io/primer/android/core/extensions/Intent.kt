package io.primer.android.core.extensions

import android.content.Intent
import android.os.Build
import java.io.Serializable

inline fun <reified T : Serializable> Intent.getSerializableCompat(name: String) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSerializableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        this.getSerializableExtra(name)
    } as? T

inline fun <reified T> Intent.getParcelableCompat(name: String) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        this.getParcelableExtra(name)
    }
