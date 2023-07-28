package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable

data class PrimerDebugOptions(val is3DSSanityCheckEnabled: Boolean = true) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (is3DSSanityCheckEnabled) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object CREATOR : Parcelable.Creator<PrimerDebugOptions> {
        override fun createFromParcel(parcel: Parcel): PrimerDebugOptions {
            return PrimerDebugOptions(parcel)
        }

        override fun newArray(size: Int): Array<PrimerDebugOptions?> {
            return arrayOfNulls(size)
        }
    }
}
