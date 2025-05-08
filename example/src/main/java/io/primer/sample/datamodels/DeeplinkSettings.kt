package io.primer.sample.datamodels

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.data.settings.DismissalMechanism
import io.primer.android.data.settings.GooglePayButtonStyle
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerStripeOptions
import java.lang.reflect.Type
import java.util.Locale

data class DeeplinkSettings(
    val paymentHandling: PrimerPaymentHandling?,
    val locale: Locale?,
    val paymentMethodOptions: PaymentMethodOptions?,
    val uiOptions: UiOptions?,
    val debugOptions: DebugOptions?,
    val clientSessionCachingEnabled: Boolean?,
    val apiVersion: PrimerApiVersion?,
) {
    data class PaymentMethodOptions(
        val android: AndroidOptions?,
        val googlePayOptions: GooglePayOptions?,
        val klarnaOptions: KlarnaOptions?,
        val threeDsOptions: ThreeDsOptions?,
        val stripeOptions: StripeOptions?,
    ) {

        data class AndroidOptions(
            val redirectScheme: String?
        )

        data class GooglePayOptions(
            val merchantName: String? = null,
            val allowedCardNetworks: List<String>?,
            val buttonStyle: GooglePayButtonStyle?,
            val captureBillingAddress: Boolean?,
            val existingPaymentMethodRequired: Boolean?,
            val shippingAddressParameters: GoogleShippingAddressParameters?,
            val requireShippingMethod: Boolean?,
            val emailAddressRequired: Boolean?,
            val buttonOptions: GooglePayButtonOptions?,
        ) {

            data class GoogleShippingAddressParameters(
                val phoneNumberRequired: Boolean?
            )

            data class GooglePayButtonOptions(
                val buttonTheme: Int?,
                val buttonType: Int?,
            )
        }

        data class KlarnaOptions(
            val recurringPaymentDescription: String?,
            val returnIntentUrl: String?,
        )

        data class ThreeDsOptions(
            val android: ThreeDsPlatformOptions?,
        ) {
            data class ThreeDsPlatformOptions(
                val threeDsAppRequestorUrl: String?
            )
        }

        data class StripeOptions(
            val mandateData: PrimerStripeOptions.MandateData?,
            val publishableKey: String?,
        ) {
            class MandateDataTypeAdapter : JsonDeserializer<PrimerStripeOptions.MandateData> {
                override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PrimerStripeOptions.MandateData {
                    val jsonObject = json.asJsonObject

                    // Check for properties to determine which subtype to use
                    if (jsonObject.has("merchantName")) {
                        val merchantName = jsonObject.get("merchantName").asString
                        return PrimerStripeOptions.MandateData.TemplateMandateData(merchantName)
                    } else if (jsonObject.has("value")) {
                        val valueElement = jsonObject.get("value")
                        // Check if value is a string or a number
                        return if (valueElement.isJsonPrimitive) {
                            val primitive = valueElement.asJsonPrimitive
                            if (primitive.isString) {
                                PrimerStripeOptions.MandateData.FullMandateStringData(primitive.asString)
                            } else if (primitive.isNumber) {
                                PrimerStripeOptions.MandateData.FullMandateData(primitive.asInt)
                            } else {
                                throw JsonParseException("Unexpected value type in MandateData")
                            }
                        } else {
                            throw JsonParseException("Value element is not a primitive type")
                        }
                    } else {
                        throw JsonParseException("Cannot determine MandateData type")
                    }
                }
            }
        }
    }

    data class UiOptions(
        val isInitScreenEnabled: Boolean?,
        val isSuccessScreenEnabled: Boolean?,
        val isErrorScreenEnabled: Boolean?,
        val dismissalMechanism: List<DismissalMechanism>?,
    )

    data class DebugOptions(
        val is3DSSanityCheckEnabled: Boolean?
    )
}
