package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.data.configuration.models.CustomerDataResponse
import io.primer.android.data.configuration.models.OrderDataResponse
import io.primer.android.extensions.readParcelable
import io.primer.android.extensions.readSerializable
import io.primer.android.ui.settings.PrimerUIOptions
import java.util.Locale

data class PrimerSettings @JvmOverloads constructor(
    var paymentHandling: PrimerPaymentHandling = PrimerPaymentHandling.AUTO,
    var locale: Locale = Locale.getDefault(),
    var paymentMethodOptions: PrimerPaymentMethodOptions = PrimerPaymentMethodOptions(),
    var uiOptions: PrimerUIOptions = PrimerUIOptions(),
    var debugOptions: PrimerDebugOptions = PrimerDebugOptions(),
    /**
     * Indicates whether client session caching is enabled.
     *
     * When set to `true`, responses from the server will be cached on the client side, allowing for faster subsequent
     * access to the same data within the cache duration. When set to `false`, every request to the server will be
     * processed without utilizing any client-side cache, ensuring that the client always receives the most up-to-date
     * data.
     * @property clientSessionCachingEnabled Boolean flag to enable or disable client session caching.
     */
    var clientSessionCachingEnabled: Boolean = false
) : Parcelable {

    internal var fromHUC: Boolean = false

    internal var order = OrderDataResponse()

    internal var customer = CustomerDataResponse()

    internal val sdkIntegrationType: SdkIntegrationType
        get() = when (fromHUC) {
            true -> SdkIntegrationType.HEADLESS
            false -> SdkIntegrationType.DROP_IN
        }

    val currency: String
        @Throws
        get() = order.currencyCode ?: throw IllegalArgumentException(CURRENCY_EXCEPTION)

    val currentAmount: Int
        @Throws
        get() = order.merchantAmount ?: order.totalOrderAmount ?: throw IllegalArgumentException(
            AMOUNT_EXCEPTION
        )

    constructor(parcel: Parcel) : this(
        paymentHandling = PrimerPaymentHandling.valueOf(parcel.readString().orEmpty()),
        locale = parcel.readSerializable<Locale>() ?: Locale.getDefault(),
        paymentMethodOptions = parcel.readParcelable<PrimerPaymentMethodOptions>() ?: PrimerPaymentMethodOptions(),
        uiOptions = parcel.readParcelable<PrimerUIOptions>() ?: PrimerUIOptions(),
        debugOptions = parcel.readParcelable<PrimerDebugOptions>() ?: PrimerDebugOptions(),
        clientSessionCachingEnabled = parcel.readByte() != 0.toByte()
    ) {
        fromHUC = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(paymentHandling.name)
        parcel.writeSerializable(locale)
        parcel.writeParcelable(paymentMethodOptions, flags)
        parcel.writeParcelable(uiOptions, flags)
        parcel.writeParcelable(debugOptions, flags)
        parcel.writeByte(if (clientSessionCachingEnabled) 1 else 0)
        parcel.writeByte(if (fromHUC) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        private const val EXCEPTION_MESSAGE = "required but not found. Please set this value"
        private const val DOCS_REFERENCE = """when generating the client session with 
POST /client-session. See documentation here: https://primer.io/docs/api#tag/Client-Session"""

        const val CURRENCY_EXCEPTION = "Currency $EXCEPTION_MESSAGE $DOCS_REFERENCE"
        const val AMOUNT_EXCEPTION = "Amount $EXCEPTION_MESSAGE $DOCS_REFERENCE"

        @JvmField
        val CREATOR = object : Parcelable.Creator<PrimerSettings> {
            override fun createFromParcel(parcel: Parcel): PrimerSettings {
                return PrimerSettings(parcel)
            }

            override fun newArray(size: Int): Array<PrimerSettings?> {
                return arrayOfNulls(size)
            }
        }
    }
}
