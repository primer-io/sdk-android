package io.primer.android.core.extensions

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RestrictTo
import java.io.Serializable

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun <reified T : Serializable> Intent.getSerializableCompat(name: String) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSerializableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        this.getSerializableExtra(name)
    } as? T

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun <reified T : Serializable> Bundle.getSerializableCompat(name: String) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSerializable(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        this.getSerializable(name)
    } as? T

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun <reified T> Intent.getParcelableCompat(name: String) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        this.getParcelableExtra(name)
    }
