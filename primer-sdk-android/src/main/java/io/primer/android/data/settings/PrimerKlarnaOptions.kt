package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable

data class PrimerKlarnaOptions @JvmOverloads constructor(
    var recurringPaymentDescription: String? = null,
    var returnIntentUrl: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(recurringPaymentDescription)
        parcel.writeString(returnIntentUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object CREATOR : Parcelable.Creator<PrimerKlarnaOptions> {
        override fun createFromParcel(parcel: Parcel): PrimerKlarnaOptions {
            return PrimerKlarnaOptions(parcel)
        }

        override fun newArray(size: Int): Array<PrimerKlarnaOptions?> {
            return arrayOfNulls(size)
        }
    }
}
