package com.example.myapplication.datamodels

import androidx.annotation.Keep

@Keep
interface ClientSession : ExampleAppRequestBody {
    val customerId: String
    val orderId: String
    val amount: Int?
    val currencyCode: String
    val order: Order
    val customer: Customer
    val paymentMethod: PaymentMethod

    @Keep
    data class Request(
        override val customerId: String,
        override val orderId: String,
        override val amount: Int? = null,
        override val currencyCode: String,
        override val order: Order,
        override val customer: Customer,
        override val paymentMethod: PaymentMethod,
    ) : ClientSession {

        companion object {

            fun build(
                customerId: String,
                orderId: String,
                amount: Int,
                countryCode: String,
                currency: String,
            ): Request {
                return Request(
                    customerId = customerId,
                    orderId = "android-test-$orderId",
                    currencyCode = currency,
                    order = Order(
                        countryCode = countryCode,
                        lineItems = listOf(
                            LineItem(
                                amount = amount,
                                quantity = 1,
                                itemId = "item-123",
                                description = "this item",
                                discountAmount = 0,
                            ),
                        ),
                    ),
                    customer = Customer(
                        emailAddress = "test@mail.com",
                        mobileNumber = "80002026",
                        firstName = "John",
                        lastName = "Doe",
                        shippingAddress = Address(
                            addressLine1 = "1 test",
                            postalCode = "12345",
                            city = "test",
                            state = "test",
                            countryCode = countryCode,
                        ),
                        billingAddress = Address(
                            firstName = "John",
                            lastName = "Doe",
                            addressLine1 = "1 test",
                            postalCode = "12345",
                            city = "test",
                            state = "test",
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
                            PAYMENT_CARD = PaymentCardOption(
                                networks = NetworkOptionGroup(
                                    VISA = NetworkOption(
                                        surcharge = SurchargeOption(
                                            amount = 100,
                                        )
                                    ),
                                    MASTERCARD = NetworkOption(
                                        surcharge = SurchargeOption(
                                            amount = 200,
                                        )
                                    ),
                                    JCB = NetworkOption(
                                        surcharge = SurchargeOption(
                                            amount = 0,
                                        )
                                    ),
                                )
                            ),
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
    ) {

        companion object {
            val configuredValues: Set<String> = setOf(
                "PAYPAL",
                "PAYMENT_CARD",
                "PROCESSOR_3DS",
                "GOOGLE_PAY",
                "ADYEN_SOFORT",
                "ADYEN_IDEAL",
                "ADYEN_GIROPAY",
                "ADYEN_TRUSTLY"
            )
        }
    }

    @Keep
    data class PaymentMethodOption(
        val surcharge: SurchargeOption,
    )

    @Keep
    data class SurchargeOption(
        val amount: Int
    )

    @Keep
    data class PaymentCardOption(
        val networks: NetworkOptionGroup
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

    @Keep
    data class CaptureData(
        val cardInformation: CardInformation,
        val billingAddress: BillingAddress,
    ) {

        data class CardInformation(
            val cardholderName: CaptureSetting
        )

        data class BillingAddress(
            val postalCode: CaptureSetting,
            val addressLine1: CaptureSetting,
        )

        data class CaptureSetting(
            val capture: Boolean,
            val required: Boolean,
        )
    }
}