package com.example.myapplication.datamodels

import androidx.annotation.Keep
import io.primer.android.model.dto.CountryCode

@Keep
interface ClientSession : ExampleAppRequestBody {
    val customerId: String
    val orderId: String
    val amount: Int?
    val currencyCode: String
    val order: Order
    val customer: Customer
    val paymentMethod: PaymentMethod

    data class Request(
        val environment: String,  // for cloud function
        override val customerId: String,
        override val orderId: String,
        override val amount: Int? = null,
        override val currencyCode: String,
        override val order: Order,
        override val customer: Customer,
        override val paymentMethod: PaymentMethod
    ) : ClientSession {

        companion object {

            fun build(): Request {
                return Request(
                    environment = "sandbox",
                    customerId = "dirk",
                    orderId = "dirk-test-10001",
                    currencyCode = "SEK",
                    amount = 123,
                    order = Order(),
                    customer = Customer(
                        emailAddress = "dirk@primer.io",
                        mobileNumber = "0841234567",
                        firstName = "John",
                        lastName = "Doe",
                        shippingAddress = Address(
                            addressLine1 = "1 test",
                            postalCode = "12345",
                            city = "test",
                            state = "test",
                            countryCode = CountryCode.SE,
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
                                )
                            ),
                        )
                    ),
                )
            }
        }
    }

    data class Response(
        val clientToken: String,
        val clientTokenExpirationDate: String,
        override val customerId: String,
        override val orderId: String,
        override val amount: Int,
        override val currencyCode: String,
        override val order: Order,
        override val customer: Customer,
        override val paymentMethod: PaymentMethod
    ) : ClientSession

    data class Order(
        val countryCode: String? = null,
        val lineItems: List<LineItem>? = null,
    )

    data class LineItem(
        val amount: Int,
        val quantity: Int,
        val itemId: String,
        val description: String,
        val discountAmount: Int,
    )

    data class Customer(
        val firstName: String? = null,
        val lastName: String? = null,
        val mobileNumber: String? = null,
        val emailAddress: String? = null,
        val nationalDocumentId: String? = null,
        val billingAddress: Address? = null,
        val shippingAddress: Address? = null,
    )

    data class Address(
        val addressLine1: String,
        val addressLine2: String? = null,
        val postalCode: String,
        val city: String,
        val state: String,
        val countryCode: CountryCode,
    )

    data class PaymentMethod(
        val vaultOnSuccess: Boolean? = null,
        val options: PaymentMethodOptionGroup,
    )

    data class PaymentMethodOptionGroup(
        val PAYPAL: PaymentMethodOption? = null,
        val PAYMENT_CARD: PaymentCardOption? = null,
        val GOOGLE_PAY: PaymentMethodOption? = null,
    )

    data class PaymentMethodOption(
        val surcharge: SurchargeOption,
    )

    data class SurchargeOption(
        val amount: Int
    )

    data class PaymentCardOption(
        val networks: NetworkOptionGroup
    )

    data class NetworkOptionGroup(
        val VISA: NetworkOption? = null,
        val MASTERCARD: NetworkOption? = null,
    )

    data class NetworkOption(
        val surcharge: SurchargeOption
    )
}