package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

data class PrimerDebugOptions(val is3DSSanityCheckEnabled: Boolean = true) : Parcelable, JSONObjectSerializable {
    constructor(parcel: Parcel) : this(parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (is3DSSanityCheckEnabled) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object CREATOR : Parcelable.Creator<PrimerDebugOptions> {
        override fun createFromParcel(parcel: Parcel): PrimerDebugOptions {
            return PrimerDebugOptions(parcel)
        }

        override fun newArray(size: Int): Array<PrimerDebugOptions?> {
            return arrayOfNulls(size)
        }

        private const val IS_3DS_SANITY_CHECK_ENABLED_FIELD = "is3DSSanityCheckEnabled"

        @JvmField
        val serializer = JSONObjectSerializer<PrimerDebugOptions> { primerDebugOptions ->
            JSONObject().apply {
                put(IS_3DS_SANITY_CHECK_ENABLED_FIELD, primerDebugOptions.is3DSSanityCheckEnabled)
            }
        }
    }
}
