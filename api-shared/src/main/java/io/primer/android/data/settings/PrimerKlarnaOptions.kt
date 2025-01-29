package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

data class PrimerKlarnaOptions
@JvmOverloads
constructor(
    var recurringPaymentDescription: String? = null,
    var returnIntentUrl: String? = null,
) : Parcelable, JSONObjectSerializable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
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

        private const val RECURRING_PAYMENT_DESCRIPTION_FIELD = "recurringPaymentDescription"
        private const val RETURN_INTENT_URL_FIELD = "returnIntentUrl"

        @JvmField
        val serializer =
            JSONObjectSerializer<PrimerKlarnaOptions> { t ->
                JSONObject().apply {
                    put(RECURRING_PAYMENT_DESCRIPTION_FIELD, t.recurringPaymentDescription)
                    put(RETURN_INTENT_URL_FIELD, t.returnIntentUrl)
                }
            }
    }
}
