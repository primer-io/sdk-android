package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.extensions.readParcelable
import io.primer.android.core.extensions.readSerializable
import io.primer.android.ui.settings.PrimerUIOptions
import org.json.JSONObject
import java.util.Locale

data class PrimerSettings
    @JvmOverloads
    constructor(
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
        var clientSessionCachingEnabled: Boolean = false,
        @ExperimentalPrimerApi var apiVersion: PrimerApiVersion = PrimerApiVersion.V2_3,
    ) : Parcelable, JSONObjectSerializable {
        var fromHUC: Boolean = false

        val sdkIntegrationType: SdkIntegrationType
            get() =
                when (fromHUC) {
                    true -> SdkIntegrationType.HEADLESS
                    false -> SdkIntegrationType.DROP_IN
                }

        constructor(parcel: Parcel) : this(
            paymentHandling = PrimerPaymentHandling.valueOf(parcel.readString().orEmpty()),
            locale = parcel.readSerializable<Locale>() ?: Locale.getDefault(),
            paymentMethodOptions = parcel.readParcelable<PrimerPaymentMethodOptions>() ?: PrimerPaymentMethodOptions(),
            uiOptions = parcel.readParcelable<PrimerUIOptions>() ?: PrimerUIOptions(),
            debugOptions = parcel.readParcelable<PrimerDebugOptions>() ?: PrimerDebugOptions(),
            clientSessionCachingEnabled = parcel.readByte() != 0.toByte(),
        ) {
            fromHUC = parcel.readByte() != 0.toByte()
            apiVersion = parcel.readString()?.let { PrimerApiVersion.valueOf(it) } ?: PrimerApiVersion.V2_3
        }

        override fun writeToParcel(
            parcel: Parcel,
            flags: Int,
        ) {
            parcel.writeString(paymentHandling.name)
            parcel.writeSerializable(locale)
            parcel.writeParcelable(paymentMethodOptions, flags)
            parcel.writeParcelable(uiOptions, flags)
            parcel.writeParcelable(debugOptions, flags)
            parcel.writeByte(if (clientSessionCachingEnabled) 1 else 0)
            parcel.writeByte(if (fromHUC) 1 else 0)
            parcel.writeString(apiVersion.name)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object {
            @JvmField
            val CREATOR =
                object : Parcelable.Creator<PrimerSettings> {
                    override fun createFromParcel(parcel: Parcel): PrimerSettings {
                        return PrimerSettings(parcel)
                    }

                    override fun newArray(size: Int): Array<PrimerSettings?> {
                        return arrayOfNulls(size)
                    }
                }

            private const val PAYMENT_HANDLING_FIELD = "paymentHandling"
            private const val LOCALE_FIELD = "locale"
            private const val PAYMENT_METHOD_OPTIONS_FIELD = "paymentMethodOptions"
            private const val UI_OPTIONS_FIELD = "uiOptions"
            private const val DEBUG_OPTIONS_FIELD = "debugOptions"
            private const val CLIENT_SESSION_CACHING_ENABLED_FIELD = "clientSessionCachingEnabled"
            private const val API_VERSION_FIELD = "apiVersion"

            @JvmField
            val serializer =
                JSONObjectSerializer<PrimerSettings> { primerSettings ->
                    JSONObject().apply {
                        put(PAYMENT_HANDLING_FIELD, primerSettings.paymentHandling.name)
                        put(LOCALE_FIELD, primerSettings.locale.toString())
                        put(
                            PAYMENT_METHOD_OPTIONS_FIELD,
                            PrimerPaymentMethodOptions.serializer.serialize(primerSettings.paymentMethodOptions),
                        )
                        put(UI_OPTIONS_FIELD, PrimerUIOptions.serializer.serialize(primerSettings.uiOptions))
                        put(DEBUG_OPTIONS_FIELD, PrimerDebugOptions.serializer.serialize(primerSettings.debugOptions))
                        put(CLIENT_SESSION_CACHING_ENABLED_FIELD, primerSettings.clientSessionCachingEnabled)
                        put(API_VERSION_FIELD, primerSettings.apiVersion.name)
                    }
                }
        }
    }
