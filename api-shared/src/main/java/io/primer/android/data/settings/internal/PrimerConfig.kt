package io.primer.android.data.settings.internal

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.data.models.DropInLoadingSource
import io.primer.android.analytics.data.models.Place
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.extensions.readParcelable
import io.primer.android.data.settings.PrimerSettings
import org.json.JSONObject

data class PrimerConfig(
    var settings: PrimerSettings = PrimerSettings()
) : Parcelable, JSONObjectSerializable {

    var clientTokenBase64: String? = null

    var intent: PrimerIntent = PrimerIntent()

    val isStandalonePaymentMethod: Boolean
        get() = intent.paymentMethodType != null

    val paymentMethodIntent: PrimerSessionIntent
        get() = intent.paymentMethodIntent

    constructor(parcel: Parcel) : this(
        parcel.readParcelable<PrimerSettings>() ?: PrimerSettings()
    ) {
        clientTokenBase64 = parcel.readString()
        intent = parcel.readParcelable<PrimerIntent>() ?: PrimerIntent()
    }

    fun toPlace() =
        if (intent.paymentMethodIntent == PrimerSessionIntent.CHECKOUT) {
            Place.UNIVERSAL_CHECKOUT
        } else {
            Place.VAULT_MANAGER
        }

    fun toDropInSource() = when {
        isStandalonePaymentMethod -> DropInLoadingSource.SHOW_PAYMENT_METHOD
        intent.paymentMethodIntent == PrimerSessionIntent.CHECKOUT -> DropInLoadingSource.UNIVERSAL_CHECKOUT
        else -> DropInLoadingSource.VAULT_MANAGER
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(settings, flags)
        parcel.writeString(clientTokenBase64)
        parcel.writeParcelable(intent, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PrimerConfig> {
        override fun createFromParcel(parcel: Parcel): PrimerConfig {
            return PrimerConfig(parcel)
        }

        override fun newArray(size: Int): Array<PrimerConfig?> {
            return arrayOfNulls(size)
        }

        private const val SETTINGS_FIELD = "settings"

        @JvmField
        val serializer = JSONObjectSerializer<PrimerConfig> { primerConfig ->
            JSONObject().apply {
                put(SETTINGS_FIELD, PrimerSettings.serializer.serialize(primerConfig.settings))
            }
        }
    }
}
