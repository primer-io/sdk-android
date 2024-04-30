package io.primer.android.ui.settings

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.extensions.readParcelable

data class PrimerUIOptions(
    var isInitScreenEnabled: Boolean = true,
    var isSuccessScreenEnabled: Boolean = true,
    var isErrorScreenEnabled: Boolean = true,
    var theme: PrimerTheme = PrimerTheme.build()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        isInitScreenEnabled = parcel.readByte() != 0.toByte(),
        isSuccessScreenEnabled = parcel.readByte() != 0.toByte(),
        isErrorScreenEnabled = parcel.readByte() != 0.toByte(),
        theme = parcel.readParcelable<PrimerTheme>() ?: PrimerTheme.build()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isInitScreenEnabled) 1 else 0)
        parcel.writeByte(if (isSuccessScreenEnabled) 1 else 0)
        parcel.writeByte(if (isErrorScreenEnabled) 1 else 0)
        parcel.writeParcelable(theme, flags)
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
