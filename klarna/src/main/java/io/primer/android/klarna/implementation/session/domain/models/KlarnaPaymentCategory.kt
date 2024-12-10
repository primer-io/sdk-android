package io.primer.android.klarna.implementation.session.domain.models

import android.os.Parcel
import android.os.Parcelable

data class KlarnaPaymentCategory(
    val identifier: String,
    val name: String,
    val descriptiveAssetUrl: String,
    val standardAssetUrl: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(identifier)
        parcel.writeString(name)
        parcel.writeString(descriptiveAssetUrl)
        parcel.writeString(standardAssetUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<KlarnaPaymentCategory> {
        override fun createFromParcel(parcel: Parcel): KlarnaPaymentCategory {
            return KlarnaPaymentCategory(parcel)
        }

        override fun newArray(size: Int): Array<KlarnaPaymentCategory?> {
            return arrayOfNulls(size)
        }
    }
}
