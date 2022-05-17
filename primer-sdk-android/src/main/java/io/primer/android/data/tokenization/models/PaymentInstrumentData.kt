package io.primer.android.data.tokenization.models

import kotlinx.serialization.Serializable

@Serializable
data class PaymentInstrumentData(
    val network: String? = null,
    val cardholderName: String? = null,
    val first6Digits: Int? = null,
    val last4Digits: Int? = null,
    val expirationMonth: Int? = null,
    val expirationYear: Int? = null,
    val gocardlessMandateId: String? = null,
    val externalPayerInfo: ExternalPayerInfo? = null,
    val klarnaCustomerToken: String? = null,
    val sessionData: SessionData? = null,
    // apaya
    val mx: String? = null,
    val mnc: Int? = null,
    val mcc: Int? = null,
    val hashedIdentifier: String? = null,
    val currencyCode: String? = null,
    val productId: String? = null,
    // async
    val paymentMethodType: String? = null,
    // bin
    val binData: BinData? = null,
)

@Serializable
data class ExternalPayerInfo(
    val email: String,
)

@Serializable
data class SessionData(
    val recurringDescription: String? = null,
    val billingAddress: BillingAddress? = null,
)

@Serializable
data class BillingAddress(
    val email: String,
)

@Serializable
data class BinData(
    val network: String? = null,
)

enum class TokenType {

    SINGLE_USE,
    MULTI_USE
}
