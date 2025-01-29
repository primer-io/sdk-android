package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.extensions.readParcelable
import org.json.JSONObject

data class PrimerPaymentMethodOptions
@JvmOverloads
constructor(
    var redirectScheme: String? = null,
    var googlePayOptions: PrimerGooglePayOptions = PrimerGooglePayOptions(),
    var klarnaOptions: PrimerKlarnaOptions = PrimerKlarnaOptions(),
    var threeDsOptions: PrimerThreeDsOptions = PrimerThreeDsOptions(),
    var stripeOptions: PrimerStripeOptions = PrimerStripeOptions(),
) : Parcelable, JSONObjectSerializable {
    constructor(parcel: Parcel) : this(
        redirectScheme = parcel.readString(),
        googlePayOptions = parcel.readParcelable<PrimerGooglePayOptions>() ?: PrimerGooglePayOptions(),
        klarnaOptions = parcel.readParcelable<PrimerKlarnaOptions>() ?: PrimerKlarnaOptions(),
        threeDsOptions = parcel.readParcelable<PrimerThreeDsOptions>() ?: PrimerThreeDsOptions(),
        stripeOptions = parcel.readParcelable<PrimerStripeOptions>() ?: PrimerStripeOptions(),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
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

        private const val REDIRECT_SCHEME_FIELD = "redirectScheme"
        private const val GOOGLE_PAY_OPTIONS_FIELD = "googlePayOptions"
        private const val KLARNA_OPTIONS_FIELD = "klarnaOptions"
        private const val THREE_DS_OPTIONS_FIELD = "threeDsOptions"
        private const val STRIPE_OPTIONS_FIELD = "stripeOptions"

        @JvmField
        val serializer =
            JSONObjectSerializer<PrimerPaymentMethodOptions> { t ->
                JSONObject().apply {
                    put(REDIRECT_SCHEME_FIELD, t.redirectScheme)
                    put(GOOGLE_PAY_OPTIONS_FIELD, PrimerGooglePayOptions.serializer.serialize(t.googlePayOptions))
                    put(KLARNA_OPTIONS_FIELD, PrimerKlarnaOptions.serializer.serialize(t.klarnaOptions))
                    put(THREE_DS_OPTIONS_FIELD, PrimerThreeDsOptions.serializer.serialize(t.threeDsOptions))
                    put(STRIPE_OPTIONS_FIELD, PrimerStripeOptions.serializer.serialize(t.stripeOptions))
                }
            }
    }
}
