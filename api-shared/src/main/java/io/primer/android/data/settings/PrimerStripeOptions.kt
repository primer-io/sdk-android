package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.extensions.readParcelable
import org.json.JSONObject

data class PrimerStripeOptions(
    val mandateData: MandateData? = null,
    val publishableKey: String? = null
) : Parcelable, JSONObjectSerializable {
    constructor(parcel: Parcel) : this(parcel.readParcelable(), parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(mandateData, flags)
        parcel.writeString(publishableKey)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object CREATOR : Parcelable.Creator<PrimerStripeOptions> {
        override fun createFromParcel(parcel: Parcel): PrimerStripeOptions {
            return PrimerStripeOptions(parcel)
        }

        override fun newArray(size: Int): Array<PrimerStripeOptions?> {
            return arrayOfNulls(size)
        }

        private const val MANDATE_DATA_FIELD = "mandateData"
        private const val PUBLISHABLE_KEY_FIELD = "publishableKey"
        private const val TEMPLATE_MANDATE_MERCHANT_NAME_FIELD = "merchantName"
        private const val FULL_MANDATE_STRING_VALUE_FIELD = "value"
        private const val FULL_MANDATE_INT_VALUE_FIELD = "intValue"

        @JvmField
        val serializer = JSONObjectSerializer<PrimerStripeOptions> { t ->
            JSONObject().apply {
                put(PUBLISHABLE_KEY_FIELD, "****")

                t.mandateData?.let { mandateData ->
                    when (mandateData) {
                        is MandateData.TemplateMandateData -> {
                            put(
                                MANDATE_DATA_FIELD,
                                JSONObject().apply {
                                    put(TEMPLATE_MANDATE_MERCHANT_NAME_FIELD, mandateData.merchantName)
                                }
                            )
                        }

                        is MandateData.FullMandateStringData -> {
                            put(
                                MANDATE_DATA_FIELD,
                                JSONObject().apply {
                                    put(FULL_MANDATE_STRING_VALUE_FIELD, mandateData.value)
                                }
                            )
                        }

                        is MandateData.FullMandateData -> {
                            put(
                                MANDATE_DATA_FIELD,
                                JSONObject().apply {
                                    put(FULL_MANDATE_INT_VALUE_FIELD, mandateData.value)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    sealed interface MandateData : Parcelable {
        data class TemplateMandateData(val merchantName: String) : MandateData {
            constructor(parcel: Parcel) : this(
                parcel.readString().orEmpty()
            )

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(merchantName)
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<TemplateMandateData> {
                override fun createFromParcel(parcel: Parcel): TemplateMandateData {
                    return TemplateMandateData(parcel)
                }

                override fun newArray(size: Int): Array<TemplateMandateData?> {
                    return arrayOfNulls(size)
                }
            }
        }

        data class FullMandateStringData(val value: String) : MandateData {
            constructor(parcel: Parcel) : this(parcel.readString().orEmpty())

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(value)
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<FullMandateStringData> {
                override fun createFromParcel(parcel: Parcel): FullMandateStringData {
                    return FullMandateStringData(parcel)
                }

                override fun newArray(size: Int): Array<FullMandateStringData?> {
                    return arrayOfNulls(size)
                }
            }
        }

        data class FullMandateData(@StringRes val value: Int) : MandateData {

            constructor(parcel: Parcel) : this(parcel.readInt())

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeInt(value)
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<FullMandateData> {

                override fun createFromParcel(parcel: Parcel): FullMandateData {
                    return FullMandateData(parcel)
                }

                override fun newArray(size: Int): Array<FullMandateData?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}
