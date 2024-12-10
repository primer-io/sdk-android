package io.primer.android.payments.core.tokenization.data.model

import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.data.tokenization.models.TokenType
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaymentMethodTokenInternalTest {

    @Test
    fun `test deserialization of PaymentMethodTokenInternal`() {
        // Given
        val json = JSONObject().apply {
            put(
                "token",
                "test_token"
            )
            put(
                "paymentInstrumentType",
                "credit_card"
            )
            put(
                "paymentMethodType",
                "visa"
            )
            put(
                "paymentInstrumentData",
                JSONObject().apply {
                    put(
                        "network",
                        "Visa"
                    )
                    put(
                        "cardholderName",
                        "John Doe"
                    )
                    put(
                        "first6Digits",
                        123456
                    )
                    put(
                        "last4Digits",
                        7890
                    )
                    put(
                        "expirationMonth",
                        12
                    )
                    put(
                        "expirationYear",
                        2025
                    )
                    put(
                        "gocardlessMandateId",
                        "gocardless123"
                    )
                    put(
                        "klarnaCustomerToken",
                        "klarnaToken123"
                    )
                }
            )
            put(
                "vaultData",
                JSONObject().apply {
                    put(
                        "customerId",
                        "customer123"
                    )
                }
            )
            put(
                "threeDSecureAuthentication",
                JSONObject().apply {
                    put(
                        "responseCode",
                        "AUTH_SUCCESS"
                    )
                    put(
                        "reasonCode",
                        "123"
                    )
                    put(
                        "reasonText",
                        "Successful authentication"
                    )
                    put(
                        "protocolVersion",
                        "1.0"
                    )
                    put(
                        "challengeIssued",
                        true
                    )
                }
            )
            put(
                "isVaulted",
                true
            )
            put(
                "analyticsId",
                "analytics123"
            )
            put(
                "tokenType",
                "SINGLE_USE"
            )
        }

        // When
        val paymentMethodTokenInternal = PaymentMethodTokenInternal.deserializer.deserialize(json)

        // Then
        assertEquals("test_token", paymentMethodTokenInternal.token)
        assertEquals("credit_card", paymentMethodTokenInternal.paymentInstrumentType)
        assertEquals("credit_card", paymentMethodTokenInternal.paymentMethodType)
        assertEquals("Visa", paymentMethodTokenInternal.paymentInstrumentData?.network)
        assertEquals("John Doe", paymentMethodTokenInternal.paymentInstrumentData?.cardholderName)
        assertEquals(123456, paymentMethodTokenInternal.paymentInstrumentData?.first6Digits)
        assertEquals(7890, paymentMethodTokenInternal.paymentInstrumentData?.last4Digits)
        assertEquals(12, paymentMethodTokenInternal.paymentInstrumentData?.expirationMonth)
        assertEquals(2025, paymentMethodTokenInternal.paymentInstrumentData?.expirationYear)
        assertEquals("klarnaToken123", paymentMethodTokenInternal.paymentInstrumentData?.klarnaCustomerToken)

        assertEquals("customer123", paymentMethodTokenInternal.vaultData?.customerId)

        assertEquals(ResponseCode.AUTH_SUCCESS, paymentMethodTokenInternal.threeDSecureAuthentication?.responseCode)
        assertEquals("123", paymentMethodTokenInternal.threeDSecureAuthentication?.reasonCode)
        assertEquals("Successful authentication", paymentMethodTokenInternal.threeDSecureAuthentication?.reasonText)
        assertEquals("1.0", paymentMethodTokenInternal.threeDSecureAuthentication?.protocolVersion)
        assertEquals(true, paymentMethodTokenInternal.threeDSecureAuthentication?.challengeIssued)

        assertEquals(true, paymentMethodTokenInternal.isVaulted)
        assertEquals("analytics123", paymentMethodTokenInternal.analyticsId)
        assertEquals(TokenType.SINGLE_USE, paymentMethodTokenInternal.tokenType)
    }

    @Test
    fun `test conversion to PrimerPaymentMethodTokenData`() {
        // Given
        val paymentMethodTokenInternal = PaymentMethodTokenInternal(
            token = "test_token",
            paymentInstrumentType = "credit_card",
            paymentMethodType = "visa",
            paymentInstrumentData = PaymentInstrumentData(
                network = "Visa",
                cardholderName = "John Doe",
                first6Digits = 123456,
                last4Digits = 7890,
                accountNumberLast4Digits = 9876,
                expirationMonth = 12,
                expirationYear = 2025,
                klarnaCustomerToken = "klarnaToken123",
                paymentMethodType = "credit_card",
                bankName = "bank_name"
            ),
            vaultData = BasePaymentToken.VaultDataResponse("customer123"),
            threeDSecureAuthentication = BasePaymentToken.AuthenticationDetailsDataResponse(
                responseCode = ResponseCode.AUTH_SUCCESS,
                reasonCode = "123",
                reasonText = "Successful authentication",
                protocolVersion = "1.0",
                challengeIssued = true
            ),
            isVaulted = true,
            analyticsId = "analytics123",
            tokenType = TokenType.SINGLE_USE
        )

        // When
        val primerPaymentMethodTokenData = paymentMethodTokenInternal.toPaymentMethodToken()

        // Then
        assertEquals("test_token", primerPaymentMethodTokenData.token)
        assertEquals("credit_card", primerPaymentMethodTokenData.paymentInstrumentType)
        assertEquals("visa", primerPaymentMethodTokenData.paymentMethodType)
        assertEquals("Visa", primerPaymentMethodTokenData.paymentInstrumentData?.network)
        assertEquals("John Doe", primerPaymentMethodTokenData.paymentInstrumentData?.cardholderName)
        assertEquals(123456, primerPaymentMethodTokenData.paymentInstrumentData?.first6Digits)
        assertEquals(7890, primerPaymentMethodTokenData.paymentInstrumentData?.last4Digits)
        assertEquals(9876, primerPaymentMethodTokenData.paymentInstrumentData?.accountNumberLast4Digits)
        assertEquals(12, primerPaymentMethodTokenData.paymentInstrumentData?.expirationMonth)
        assertEquals(2025, primerPaymentMethodTokenData.paymentInstrumentData?.expirationYear)
        assertEquals("klarnaToken123", primerPaymentMethodTokenData.paymentInstrumentData?.klarnaCustomerToken)
        assertEquals("bank_name", primerPaymentMethodTokenData.paymentInstrumentData?.bankName)

        assertEquals("customer123", primerPaymentMethodTokenData.vaultData?.customerId)

        assertEquals(ResponseCode.AUTH_SUCCESS, primerPaymentMethodTokenData.threeDSecureAuthentication?.responseCode)
        assertEquals("123", primerPaymentMethodTokenData.threeDSecureAuthentication?.reasonCode)
        assertEquals("Successful authentication", primerPaymentMethodTokenData.threeDSecureAuthentication?.reasonText)
        assertEquals("1.0", primerPaymentMethodTokenData.threeDSecureAuthentication?.protocolVersion)
        assertEquals(true, primerPaymentMethodTokenData.threeDSecureAuthentication?.challengeIssued)

        assertEquals(true, primerPaymentMethodTokenData.isVaulted)
        assertEquals("analytics123", primerPaymentMethodTokenData.analyticsId)
        assertEquals(TokenType.SINGLE_USE, primerPaymentMethodTokenData.tokenType)
    }
}
