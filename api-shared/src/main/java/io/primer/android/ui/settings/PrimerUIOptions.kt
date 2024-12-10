package io.primer.android.ui.settings

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.extensions.readParcelable
import org.json.JSONObject

data class PrimerUIOptions(
    var isInitScreenEnabled: Boolean = true,
    var isSuccessScreenEnabled: Boolean = true,
    var isErrorScreenEnabled: Boolean = true,
    var theme: PrimerTheme = PrimerTheme.build()
) : Parcelable, JSONObjectSerializable {
    constructor(parcel: Parcel) : this(
        isInitScreenEnabled = parcel.readByte() != 0.toByte(),
        isSuccessScreenEnabled = parcel.readByte() != 0.toByte(),
        isErrorScreenEnabled = parcel.readByte() != 0.toByte(),
        theme = parcel.readParcelable<PrimerTheme>() ?: PrimerTheme.build()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isInitScreenEnabled) 1 else 0)
        parcel.writeByte(if (isSuccessScreenEnabled) 1 else 0)
        parcel.writeByte(if (isErrorScreenEnabled) 1 else 0)
        parcel.writeParcelable(theme, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object CREATOR : Parcelable.Creator<PrimerUIOptions> {
        override fun createFromParcel(parcel: Parcel): PrimerUIOptions {
            return PrimerUIOptions(parcel)
        }

        override fun newArray(size: Int): Array<PrimerUIOptions?> {
            return arrayOfNulls(size)
        }

        private const val INIT_SCREEN_ENABLED_FIELD = "isInitScreenEnabled"
        private const val SUCCESS_SCREEN_ENABLED_FIELD = "isSuccessScreenEnabled"
        private const val ERROR_SCREEN_ENABLED_FIELD = "isErrorScreenEnabled"
        private const val THEME_FIELD = "theme"

        @JvmField
        val serializer = JSONObjectSerializer<PrimerUIOptions> { t ->
            JSONObject().apply {
                put(INIT_SCREEN_ENABLED_FIELD, t.isInitScreenEnabled)
                put(SUCCESS_SCREEN_ENABLED_FIELD, t.isSuccessScreenEnabled)
                put(ERROR_SCREEN_ENABLED_FIELD, t.isErrorScreenEnabled)
                put(THEME_FIELD, PrimerTheme.serializer.serialize(t.theme))
            }
        }
    }
}
