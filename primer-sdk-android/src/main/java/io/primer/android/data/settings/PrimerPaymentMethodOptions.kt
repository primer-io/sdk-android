package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.extensions.readParcelable

data class PrimerPaymentMethodOptions @JvmOverloads constructor(
    val redirectScheme: String? = null,
    val googlePayOptions: PrimerGooglePayOptions = PrimerGooglePayOptions(),
    val klarnaOptions: PrimerKlarnaOptions = PrimerKlarnaOptions(),
    val threeDsOptions: PrimerThreeDsOptions = PrimerThreeDsOptions(),
    val stripeOptions: PrimerStripeOptions = PrimerStripeOptions()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        redirectScheme = parcel.readString(),
        googlePayOptions = parcel.readParcelable<PrimerGooglePayOptions>() ?: PrimerGooglePayOptions(),
        klarnaOptions = parcel.readParcelable<PrimerKlarnaOptions>() ?: PrimerKlarnaOptions(),
        threeDsOptions = parcel.readParcelable<PrimerThreeDsOptions>() ?: PrimerThreeDsOptions(),
        stripeOptions = parcel.readParcelable<PrimerStripeOptions>() ?: PrimerStripeOptions()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(redirectScheme)
        parcel.writeParcelable(googlePayOptions, flags)
        parcel.writeParcelable(klarnaOptions, flags)
        parcel.writeParcelable(threeDsOptions, flags)
        parcel.writeParcelable(stripeOptions, flags)
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
