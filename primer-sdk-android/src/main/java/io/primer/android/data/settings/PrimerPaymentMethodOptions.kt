package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.extensions.readParcelable

data class PrimerPaymentMethodOptions @JvmOverloads constructor(
    var redirectScheme: String? = null,
    var googlePayOptions: PrimerGooglePayOptions = PrimerGooglePayOptions(),
    var klarnaOptions: PrimerKlarnaOptions = PrimerKlarnaOptions(),
    var threeDsOptions: PrimerThreeDsOptions = PrimerThreeDsOptions()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable<PrimerGooglePayOptions>() ?: PrimerGooglePayOptions(),
        parcel.readParcelable<PrimerKlarnaOptions>() ?: PrimerKlarnaOptions(),
        parcel.readParcelable<PrimerThreeDsOptions>() ?: PrimerThreeDsOptions()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(redirectScheme)
        parcel.writeParcelable(googlePayOptions, flags)
        parcel.writeParcelable(klarnaOptions, flags)
        parcel.writeParcelable(threeDsOptions, flags)
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
