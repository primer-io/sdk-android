package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

data class PrimerThreeDsOptions(val threeDsAppRequestorUrl: String? = null) : Parcelable, JSONObjectSerializable {
    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
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

        private const val THREE_DS_APP_REQUESTOR_URL_FIELD = "threeDsAppRequestorUrl"

        @JvmField
        val serializer =
            JSONObjectSerializer<PrimerThreeDsOptions> { t ->
                JSONObject().apply {
                    put(THREE_DS_APP_REQUESTOR_URL_FIELD, t.threeDsAppRequestorUrl)
                }
            }
    }
}
