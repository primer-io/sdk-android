package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable

data class PrimerThreeDsOptions(val threeDsAppRequestorUrl: String? = null) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(threeDsAppRequestorUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object CREATOR : Parcelable.Creator<PrimerThreeDsOptions> {
        override fun createFromParcel(parcel: Parcel): PrimerThreeDsOptions {
            return PrimerThreeDsOptions(parcel)
        }

        override fun newArray(size: Int): Array<PrimerThreeDsOptions?> {
            return arrayOfNulls(size)
        }
    }
}
