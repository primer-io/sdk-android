package io.primer.android.data.settings.internal

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.PrimerSessionIntent

internal data class PrimerIntent(
    val paymentMethodIntent: PrimerSessionIntent = PrimerSessionIntent.CHECKOUT,
    val paymentMethodType: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        PrimerSessionIntent.valueOf(parcel.readString().orEmpty()),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(paymentMethodIntent.name)
        parcel.writeString(paymentMethodType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PrimerIntent> {
        override fun createFromParcel(parcel: Parcel): PrimerIntent {
            return PrimerIntent(parcel)
        }

        override fun newArray(size: Int): Array<PrimerIntent?> {
            return arrayOfNulls(size)
        }
    }
}
