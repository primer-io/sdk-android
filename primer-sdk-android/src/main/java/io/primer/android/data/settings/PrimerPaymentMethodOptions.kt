package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable

data class PrimerPaymentMethodOptions(
    var redirectScheme: String? = null,
    var googlePayOptions: PrimerGooglePayOptions = PrimerGooglePayOptions(),
    var klarnaOptions: PrimerKlarnaOptions = PrimerKlarnaOptions(),
    var apayaOptions: PrimerApayaOptions = PrimerApayaOptions(),
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(PrimerGooglePayOptions::class.java.classLoader)
            ?: PrimerGooglePayOptions(),
        parcel.readParcelable(PrimerKlarnaOptions::class.java.classLoader)
            ?: PrimerKlarnaOptions(),
        parcel.readParcelable(PrimerApayaOptions::class.java.classLoader)
            ?: PrimerApayaOptions()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(redirectScheme)
        parcel.writeParcelable(googlePayOptions, flags)
        parcel.writeParcelable(klarnaOptions, flags)
        parcel.writeParcelable(apayaOptions, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object CREATOR : Parcelable.Creator<PrimerPaymentMethodOptions> {
        override fun createFromParcel(parcel: Parcel): PrimerPaymentMethodOptions {
            return PrimerPaymentMethodOptions(parcel)
        }

        override fun newArray(size: Int): Array<PrimerPaymentMethodOptions?> {
            return arrayOfNulls(size)
        }
    }
}
