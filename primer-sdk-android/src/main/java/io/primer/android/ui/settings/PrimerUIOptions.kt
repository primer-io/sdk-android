package io.primer.android.ui.settings

import android.os.Parcel
import android.os.Parcelable

data class PrimerUIOptions(
    var isInitScreenEnabled: Boolean = true,
    var isSuccessScreenEnabled: Boolean = true,
    var isErrorScreenEnabled: Boolean = true,
    var theme: PrimerTheme = PrimerTheme.build()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        PrimerTheme.build()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isInitScreenEnabled) 1 else 0)
        parcel.writeByte(if (isSuccessScreenEnabled) 1 else 0)
        parcel.writeByte(if (isErrorScreenEnabled) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object CREATOR : Parcelable.Creator<PrimerUIOptions> {
        override fun createFromParcel(parcel: Parcel): PrimerUIOptions {
            return PrimerUIOptions(parcel)
        }

        override fun newArray(size: Int): Array<PrimerUIOptions?> {
            return arrayOfNulls(size)
        }
    }
}
