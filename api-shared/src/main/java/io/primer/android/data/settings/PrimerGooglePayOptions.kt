package io.primer.android.data.settings

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.wallet.button.ButtonConstants
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils.serialize
import io.primer.android.core.extensions.readParcelable
import org.json.JSONArray
import org.json.JSONObject

enum class GooglePayButtonStyle {
    WHITE,
    BLACK
}

data class GooglePayButtonOptions(
    val buttonTheme: Int = ButtonConstants.ButtonTheme.DARK,
    val buttonType: Int = ButtonConstants.ButtonType.PAY
) : Parcelable, JSONObjectSerializable {

    constructor(parcel: Parcel) : this(
        buttonTheme = parcel.readInt(),
        buttonType = parcel.readInt()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(buttonTheme)
        dest.writeInt(buttonType)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object {

        private const val BUTTON_THEME_FIELD = "buttonTheme"
        private const val BUTTON_TYPE_FIELD = "buttonType"

        @JvmField
        val serializer = JSONObjectSerializer<GooglePayButtonOptions> { t ->
            JSONObject().apply {
                put(BUTTON_THEME_FIELD, t.buttonTheme)
                put(BUTTON_TYPE_FIELD, t.buttonType)
            }
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<GooglePayButtonOptions> {
            override fun createFromParcel(parcel: Parcel): GooglePayButtonOptions {
                return GooglePayButtonOptions(parcel)
            }

            override fun newArray(size: Int): Array<GooglePayButtonOptions?> {
                return arrayOfNulls(size)
            }
        }
    }
}

data class PrimerGooglePayOptions @JvmOverloads constructor(
    var merchantName: String? = null,
    @Deprecated(DEPRECATION_MESSAGE)
    var allowedCardNetworks: List<String> = listOf(
        "AMEX",
        "DISCOVER",
        "JCB",
        "MASTERCARD",
        "VISA"
    ),
    var buttonStyle: GooglePayButtonStyle = GooglePayButtonStyle.BLACK,
    var captureBillingAddress: Boolean = false,
    val existingPaymentMethodRequired: Boolean = false,
    val shippingAddressParameters: PrimerGoogleShippingAddressParameters? = null,
    val requireShippingMethod: Boolean = false,
    val emailAddressRequired: Boolean = false,
    val buttonOptions: GooglePayButtonOptions = GooglePayButtonOptions()
) : Parcelable, JSONObjectSerializable {
    constructor(parcel: Parcel) : this(
        merchantName = parcel.readString(),
        allowedCardNetworks = parcel.createStringArrayList().orEmpty(),
        buttonStyle = GooglePayButtonStyle.valueOf(parcel.readString().orEmpty()),
        captureBillingAddress = parcel.readByte() != 0.toByte(),
        existingPaymentMethodRequired = parcel.readByte() != 0.toByte(),
        shippingAddressParameters = parcel.readParcelable<PrimerGoogleShippingAddressParameters>(),
        requireShippingMethod = parcel.readByte() != 0.toByte(),
        emailAddressRequired = parcel.readByte() != 0.toByte(),
        buttonOptions = parcel.readParcelable<GooglePayButtonOptions>() ?: GooglePayButtonOptions()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(merchantName)
        parcel.writeStringList(allowedCardNetworks)
        parcel.writeString(buttonStyle.name)
        parcel.writeByte(if (captureBillingAddress) 1 else 0)
        parcel.writeByte(if (existingPaymentMethodRequired) 1 else 0)
        parcel.writeParcelable(shippingAddressParameters, flags)
        parcel.writeByte(if (requireShippingMethod) 1 else 0)
        parcel.writeByte(if (emailAddressRequired) 1 else 0)
        parcel.writeParcelable(buttonOptions, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object {

        const val DEPRECATION_MESSAGE = """
        This field has been deprecated, and modifying its settings will no longer have any impact.
        To set allowed card networks pass 'orderedAllowedCardNetworks` when creating client session
        (https://apiref.primer.io/reference/create_client_side_token_client_session_post)."
    """

        @JvmField
        val CREATOR = object : Parcelable.Creator<PrimerGooglePayOptions> {
            override fun createFromParcel(parcel: Parcel): PrimerGooglePayOptions {
                return PrimerGooglePayOptions(parcel)
            }

            override fun newArray(size: Int): Array<PrimerGooglePayOptions?> {
                return arrayOfNulls(size)
            }
        }

        private const val MERCHANT_NAME_FIELD = "merchantName"
        private const val ALLOWED_CARD_NETWORKS_FIELD = "allowedCardNetworks"
        private const val BUTTON_STYLE_FIELD = "buttonStyle"
        private const val CAPTURE_BILLING_ADDRESS_FIELD = "captureBillingAddress"
        private const val EXISTING_PAYMENT_METHOD_REQUIRED_FIELD = "existingPaymentMethodRequired"
        private const val SHIPPING_ADDRESS_PARAMS_FIELD = "shippingAddressParameters"
        private const val REQUIRE_SHIPPING_METHOD_FIELD = "requireShippingMethod"
        private const val EMAIL_ADDRESS_REQUIRED_FIELD = "emailAddressRequired"
        private const val BUTTON_OPTIONS_FIELD = "buttonOptions"

        @JvmField
        val serializer = JSONObjectSerializer<PrimerGooglePayOptions> { t ->
            JSONObject().apply {
                put(MERCHANT_NAME_FIELD, t.merchantName)
                put(ALLOWED_CARD_NETWORKS_FIELD, JSONArray(t.allowedCardNetworks))
                put(BUTTON_STYLE_FIELD, t.buttonStyle.name)
                put(CAPTURE_BILLING_ADDRESS_FIELD, t.captureBillingAddress)
                put(EXISTING_PAYMENT_METHOD_REQUIRED_FIELD, t.existingPaymentMethodRequired)
                put(SHIPPING_ADDRESS_PARAMS_FIELD, t.shippingAddressParameters?.serialize())
                put(REQUIRE_SHIPPING_METHOD_FIELD, t.requireShippingMethod)
                put(EMAIL_ADDRESS_REQUIRED_FIELD, t.emailAddressRequired)
                put(BUTTON_OPTIONS_FIELD, t.buttonOptions.serialize())
            }
        }
    }
}

data class PrimerGoogleShippingAddressParameters(
    val phoneNumberRequired: Boolean = false
) : Parcelable, JSONObjectSerializable {

    constructor(parcel: Parcel) : this(
        phoneNumberRequired = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (phoneNumberRequired) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object {

        private const val PHONE_NUMBER_REQUIRED_FIELD = "phoneNumberRequired"

        @JvmField
        val serializer = JSONObjectSerializer<PrimerGoogleShippingAddressParameters> { t ->
            JSONObject().apply {
                put(PHONE_NUMBER_REQUIRED_FIELD, t.phoneNumberRequired)
            }
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<PrimerGoogleShippingAddressParameters> {
            override fun createFromParcel(parcel: Parcel): PrimerGoogleShippingAddressParameters {
                return PrimerGoogleShippingAddressParameters(parcel)
            }

            override fun newArray(size: Int): Array<PrimerGoogleShippingAddressParameters?> {
                return arrayOfNulls(size)
            }
        }
    }
}
