package io.primer.android.data.settings.internal

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.data.models.Place
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.model.MonetaryAmount

internal data class PrimerConfig(
    var settings: PrimerSettings = PrimerSettings(),
) : Parcelable {

    internal var clientTokenBase64: String? = null

    internal var intent: PrimerIntent = PrimerIntent()

    internal val isStandalonePaymentMethod: Boolean
        get() = intent.paymentMethodType != null

    internal val monetaryAmount: MonetaryAmount?
        get() {
            val currency = settings.order.currencyCode
            val amount = settings.currentAmount
            return MonetaryAmount.create(currency, amount)
        }

    internal val paymentMethodIntent: PrimerSessionIntent
        get() = intent.paymentMethodIntent

    constructor(parcel: Parcel) : this(
        parcel.readParcelable<PrimerSettings>(PrimerSettings::class.java.classLoader)
            ?: PrimerSettings()
    ) {
        clientTokenBase64 = parcel.readString()
        intent = parcel.readParcelable(PrimerIntent::class.java.classLoader) ?: PrimerIntent()
    }

    internal fun getMonetaryAmountWithSurcharge(): MonetaryAmount? {
        val amount = settings.currentAmount
        val currency = settings.order.currencyCode
        return MonetaryAmount.create(currency, amount)
    }

    internal fun toPlace() =
        if (intent.paymentMethodIntent == PrimerSessionIntent.CHECKOUT)
            Place.UNIVERSAL_CHECKOUT else Place.VAULT_MANAGER

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(settings, flags)
        parcel.writeString(clientTokenBase64)
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
    }
}
