package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable

enum class GooglePayButtonStyle {
    WHITE,
    BLACK
}

data class PrimerGooglePayOptions @JvmOverloads constructor(
    var merchantName: String? = null,
    @Deprecated(DEPRECATION_MESSAGE)
    var allowedCardNetworks: List<String> = listOf(
        "AMEX",
        "DISCOVER",
        "JCB",
        "MASTERCARD",
        "VISA"
    ),
    var buttonStyle: GooglePayButtonStyle = GooglePayButtonStyle.BLACK,
    var captureBillingAddress: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.createStringArrayList().orEmpty(),
        GooglePayButtonStyle.valueOf(parcel.readString().orEmpty()),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(merchantName)
        parcel.writeStringList(allowedCardNetworks)
        parcel.writeString(buttonStyle.name)
        parcel.writeByte(if (captureBillingAddress) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object {

        const val DEPRECATION_MESSAGE = """
        This field has been deprecated, and modifying its settings will no longer have any impact.
        To set allowed card networks pass 'orderedAllowedCardNetworks` when creating client session
        (https://apiref.primer.io/reference/create_client_side_token_client_session_post)."
    """

        @JvmField
        val CREATOR = object : Parcelable.Creator<PrimerGooglePayOptions> {
            override fun createFromParcel(parcel: Parcel): PrimerGooglePayOptions {
                return PrimerGooglePayOptions(parcel)
            }

            override fun newArray(size: Int): Array<PrimerGooglePayOptions?> {
                return arrayOfNulls(size)
            }
        }
    }
}
