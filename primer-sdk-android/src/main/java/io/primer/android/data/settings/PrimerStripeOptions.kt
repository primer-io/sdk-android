package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import io.primer.android.extensions.readParcelable

data class PrimerStripeOptions(
    val mandateData: MandateData? = null,
    val publishableKey: String? = null
) : Parcelable {
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
