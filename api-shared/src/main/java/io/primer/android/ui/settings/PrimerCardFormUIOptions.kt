package io.primer.android.ui.settings

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

data class PrimerCardFormUIOptions(
    var payButtonAddNewCard: Boolean = false,
) : Parcelable, JSONObjectSerializable {
    constructor(parcel: Parcel) : this(
        payButtonAddNewCard = parcel.readByte() != 0.toByte(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (payButtonAddNewCard) 1 else 0)
    }

    override fun describeContents(): Int = 0

    internal companion object CREATOR : Parcelable.Creator<PrimerCardFormUIOptions> {

        override fun createFromParcel(parcel: Parcel): PrimerCardFormUIOptions {
            return PrimerCardFormUIOptions(parcel)
        }

        override fun newArray(size: Int): Array<PrimerCardFormUIOptions?> {
            return arrayOfNulls(size)
        }

        private const val PAY_BUTTON_ADD_NEW_CARD_FIELD = "payButtonAddNewCard"

        @JvmField
        val serializer = JSONObjectSerializer<PrimerCardFormUIOptions> { t ->
            JSONObject().apply {
                put(PAY_BUTTON_ADD_NEW_CARD_FIELD, t.payButtonAddNewCard)
            }
        }
    }
}
