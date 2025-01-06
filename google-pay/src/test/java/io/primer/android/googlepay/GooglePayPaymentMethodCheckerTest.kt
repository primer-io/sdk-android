package io.primer.android.googlepay

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.configuration.data.model.CardNetwork
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GooglePayPaymentMethodCheckerTest {
    private lateinit var googlePayFacade: GooglePayFacade
    private lateinit var checker: GooglePayPaymentMethodChecker
    private lateinit var googlePay: GooglePay

    @BeforeEach
    fun setUp() {
        googlePayFacade = mockk()
        checker = GooglePayPaymentMethodChecker(googlePayFacade)
        googlePay =
            GooglePay(
                totalPrice = "totalPrice",
                countryCode = "countryCode",
                currencyCode = "currencyCode",
                allowedCardNetworks = listOf(CardNetwork.Type.VISA, CardNetwork.Type.MASTERCARD),
                billingAddressRequired = true,
                existingPaymentMethodRequired = false,
            )
    }

    @Test
    fun `shouldPaymentMethodBeAvailable returns true when Google Pay is ready to pay`() =
        runTest {
            coEvery {
                googlePayFacade.checkIfIsReadyToPay(
                    googlePay.allowedCardNetworks.map { it.name },
                    googlePay.allowedCardAuthMethods,
                    googlePay.billingAddressRequired,
                    googlePay.existingPaymentMethodRequired,
                )
            } returns true

            val result = checker.shouldPaymentMethodBeAvailable(googlePay)

            assertTrue(result)
            coVerify {
                googlePayFacade.checkIfIsReadyToPay(
                    googlePay.allowedCardNetworks.map { it.name },
                    googlePay.allowedCardAuthMethods,
                    googlePay.billingAddressRequired,
                    googlePay.existingPaymentMethodRequired,
                )
            }
        }

    @Test
    fun `shouldPaymentMethodBeAvailable returns false when Google Pay is not ready to pay`() =
        runTest {
            coEvery {
                googlePayFacade.checkIfIsReadyToPay(
                    googlePay.allowedCardNetworks.map { it.name },
                    googlePay.allowedCardAuthMethods,
                    googlePay.billingAddressRequired,
                    googlePay.existingPaymentMethodRequired,
                )
            } returns false

            val result = checker.shouldPaymentMethodBeAvailable(googlePay)

            assertFalse(result)
            coVerify {
                googlePayFacade.checkIfIsReadyToPay(
                    googlePay.allowedCardNetworks.map { it.name },
                    googlePay.allowedCardAuthMethods,
                    googlePay.billingAddressRequired,
                    googlePay.existingPaymentMethodRequired,
                )
            }
        }
}
