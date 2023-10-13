package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable

@Deprecated("This class is deprecated and will be removed in future release.")
data class PrimerApayaOptions(
    var webViewTitle: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(webViewTitle)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object CREATOR : Parcelable.Creator<PrimerApayaOptions> {
        override fun createFromParcel(parcel: Parcel): PrimerApayaOptions {
            return PrimerApayaOptions(parcel)
        }

        override fun newArray(size: Int): Array<PrimerApayaOptions?> {
            return arrayOfNulls(size)
        }
    }
}
