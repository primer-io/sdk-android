package io.primer.android.extensions

import android.os.Build
import android.os.Parcel

internal inline fun <reified T> Parcel.readSerializable() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.readSerializable(T::class.java.classLoader, T::class.java)
    } else {
        @Suppress("DEPRECATION") this.readSerializable() as? T
    }

internal inline fun <reified T> Parcel.readParcelable() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.readParcelable(T::class.java.classLoader, T::class.java)
    } else {
        @Suppress("DEPRECATION") this.readParcelable(T::class.java.classLoader)
    }
