package io.primer.sample.datamodels

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
interface ClientSession : ExampleAppRequestBody {
    val customerId: String
    val orderId: String
    val amount: Int?
    val currencyCode: String
    val order: Order
    val customer: Customer
    val paymentMethod: PaymentMethod
    val metadata: Map<String, Any>?

    @Keep
    data class Request(
        override val customerId: String,
        override val orderId: String,
        override val amount: Int? = null,
        override val currencyCode: String,
        override val order: Order,
        override val customer: Customer,
        override val paymentMethod: PaymentMethod,
        override val metadata: Map<String, Any>? = null
    ) : ClientSession {

        companion object {

            fun build(
                customerId: String,
                orderId: String,
                amount: Int,
                countryCode: String,
                currency: String,
                metadata: String?,
                captureVaultedCardCvv: Boolean
            ): Request {
                var metadataMap: MutableMap<String, Any>? = null
                if (!metadata.isNullOrEmpty() && metadata.contains(":")) {
                    val values = metadata.split(":")
                    if (values.size % 2 == 0) {
                        metadataMap = mutableMapOf()
                        values.forEachIndexed { index, data ->
                            if (index == 0 || index % 2 == 0) {
                                metadataMap[data] = values[index + 1]
                            }
                        }
                    }
                }
                return Request(
                    customerId = customerId,
                    orderId = "android-test-$orderId",
                    currencyCode = currency,
                    metadata = (metadataMap ?: mutableMapOf()).apply {
                        put("deviceInfo", buildMap {
                            put("ipAddress", "127.0.0.1")
                            put("userAgent", "Android")
                        })
                        put("scenario", "STRIPE_ACH_ONEOFF")
                    },
                    order = Order(
                        countryCode = countryCode,
                        lineItems = listOf(
                            LineItem(
                                amount = amount,
                                quantity = 1,
                                itemId = "item-123",
                                description = "this item",
                                discountAmount = 0,
                                taxAmount = 0,
                                productType = "PHYSICAL"
                            ),
                        ),
                    ),
                    customer = Customer(
                        emailAddress = "customer@email.de",
                        mobileNumber = "+4901761428434",
                        firstName = "John",
                        lastName = "Doe",
                        shippingAddress = Address(
                            firstName = "John",
                            lastName = "Doe",
                            addressLine1 = "Neue Schönhauser Str. 2",
                            postalCode = "10178",
                            city = "Berlin",
                            state = "Berlin",
                            countryCode = countryCode,
                        ),
                        billingAddress = Address(
                            firstName = "John",
                            lastName = "Doe",
                            addressLine1 = "Neue Schönhauser Str. 2",
                            postalCode = "10178",
                            city = "Berlin",
                            state = "Berlin",
                            countryCode = countryCode,
                        ),
                        nationalDocumentId = "9011211234567",
                    ),
                    paymentMethod = PaymentMethod(
                        options = PaymentMethodOptionGroup(
                            PAYPAL = PaymentMethodOption(
                                surcharge = SurchargeOption(
                                    amount = 50,
                                )
                            ),
                            GOOGLE_PAY = PaymentMethodOption(
                                surcharge = SurchargeOption(
                                    amount = 60,
                                )
                            ),
                            ADYEN_SOFORT = PaymentMethodOption(
                                surcharge = SurchargeOption(
                                    amount = 150,
                                )
                            ),
                            ADYEN_IDEAL = PaymentMethodOption(
                                surcharge = SurchargeOption(
                                    amount = 120,
                                )
                            ),
                            ADYEN_GIROPAY = PaymentMethodOption(
                                surcharge = SurchargeOption(
                                    amount = 130,
                                )
                            ),
                            ADYEN_TRUSTLY = PaymentMethodOption(
                                surcharge = SurchargeOption(
                                    amount = 140,
                                )
                            ),
                            KLARNA = PaymentMethodOption(
                                surcharge = SurchargeOption(
                                    amount = 140,
                                ),
                                extraMerchantData = JSONObject(
                                    """
                                    {
                                       "customer_account_info":[
                                          {
                                             "unique_account_identifier":"Adam_Adamsson",
                                             "account_registration_date":"2020-11-24T15:00",
                                             "account_last_modified":"2020-11-24T15:00"
                                          }
                                       ]
                                    }
                                    """.trimIndent()
                                )
                            ),
                            PAYMENT_CARD = PaymentCardOption(
                                networks = NetworkOptionGroup(
                                    JCB = NetworkOption(
                                        surcharge = SurchargeOption(
                                            amount = 0,
                                        )
                                    ),
                                ),
                                captureVaultedCardCvv = captureVaultedCardCvv
                            ),
                        ),
                        descriptor = "test-descriptor",
                        orderedAllowedCardNetworks = listOf(
                            "DANKORT",
                            "MASTERCARD",
                            "CARTES_BANCAIRES",
                            "VISA",
                            "AMEX",
                            "OTHER"
                        )
                    ),
                )
            }
        }
    }

    @Keep
    data class Response(
        val clientToken: String,
        val clientTokenExpirationDate: String,
        override val customerId: String,
        override val orderId: String,
        override val amount: Int,
        override val currencyCode: String,
        override val order: Order,
        override val customer: Customer,
        override val paymentMethod: PaymentMethod,
        override val metadata: Map<String, String>? = null
    ) : ClientSession

    @Keep
    data class Order(
        val countryCode: String? = null,
        val lineItems: List<LineItem>? = null,
    )

    @Keep
    data class LineItem(
        val amount: Int,
        val quantity: Int,
        val itemId: String,
        val description: String,
        val discountAmount: Int,
        val taxAmount: Int,
        val productType: String
    )

    @Keep
    data class Customer(
        val firstName: String? = null,
        val lastName: String? = null,
        val mobileNumber: String? = null,
        val emailAddress: String? = null,
        val nationalDocumentId: String? = null,
        val billingAddress: Address? = null,
        val shippingAddress: Address? = null,
    )

    @Keep
    data class Address(
        val firstName: String? = null,
        val lastName: String? = null,
        val addressLine1: String,
        val addressLine2: String? = null,
        val postalCode: String,
        val city: String,
        val state: String,
        val countryCode: String,
    )

    @Keep
    data class PaymentMethod(
        val vaultOnSuccess: Boolean? = null,
        val options: PaymentMethodOptionGroup,
        val descriptor: String,
        val orderedAllowedCardNetworks: List<String>
    )

    @Keep
    data class PaymentMethodOptionGroup(
        val PAYPAL: PaymentMethodOption? = null,
        val PAYMENT_CARD: PaymentCardOption? = null,
        val PROCESSOR_3DS: PaymentMethodOption? = null,
        val GOOGLE_PAY: PaymentMethodOption? = null,
        val ADYEN_SOFORT: PaymentMethodOption? = null,
        val ADYEN_IDEAL: PaymentMethodOption? = null,
        val ADYEN_GIROPAY: PaymentMethodOption? = null,
        val ADYEN_TRUSTLY: PaymentMethodOption? = null,
        val KLARNA: PaymentMethodOption? = null,
    )

    @Keep
    data class PaymentMethodOption(
        val surcharge: SurchargeOption,
        val extraMerchantData: JSONObject? = null
    )

    @Keep
    data class SurchargeOption(
        val amount: Int
    )

    @Keep
    data class PaymentCardOption(
        val networks: NetworkOptionGroup,
        val captureVaultedCardCvv: Boolean
    )

    @Keep
    data class NetworkOptionGroup(
        val VISA: NetworkOption? = null,
        val MASTERCARD: NetworkOption? = null,
        val JCB: NetworkOption? = null,
    )

    @Keep
    data class NetworkOption(
        val surcharge: SurchargeOption
    )
}
