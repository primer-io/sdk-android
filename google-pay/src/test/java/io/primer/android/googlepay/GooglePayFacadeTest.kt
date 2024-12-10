package io.primer.android.googlepay

import android.app.Activity
import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.data.settings.PrimerGoogleShippingAddressParameters
import io.primer.android.configuration.data.model.ShippingMethod
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.core.logging.internal.LogReporter
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GooglePayFacadeTest {

    private lateinit var paymentsClient: PaymentsClient
    private lateinit var logReporter: LogReporter
    private lateinit var googlePayFacade: GooglePayFacade
    private lateinit var context: Context

    @BeforeEach
    fun setUp() {
        context = mockk()
        paymentsClient = mockk(relaxed = true) {
            every { applicationContext } returns context
        }
        logReporter = mockk(relaxed = true)
        googlePayFacade = GooglePayFacade(paymentsClient, logReporter)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(GoogleApiAvailability::class)
    }

    @Test
    fun `checkIfIsReadyToPay returns true when Google Play services are available`() = runBlocking {
        mockkStatic(GoogleApiAvailability::class)
        every {
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        } returns ConnectionResult.SUCCESS

        val task = mockk<Task<Boolean>>()
        every { paymentsClient.isReadyToPay(any()) } returns task
        every { task.addOnCompleteListener(any()) } answers {
            val listener = arg<OnCompleteListener<Boolean>>(0)
            listener.onComplete(task)
            task
        }
        every { task.getResult(ApiException::class.java) } returns true

        val result = googlePayFacade.checkIfIsReadyToPay(emptyList(), emptyList(), true, false)

        assertTrue(result)
    }

    @Test
    fun `checkIfIsReadyToPay returns false when Google Play services are not available`() = runBlocking {
        mockkStatic(GoogleApiAvailability::class)
        every {
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        } returns ConnectionResult.SERVICE_MISSING

        val result = googlePayFacade.checkIfIsReadyToPay(emptyList(), emptyList(), true, false)

        assertFalse(result)
    }

    @Test
    fun `checkIfIsReadyToPay returns false when ApiException is thrown`() = runBlocking {
        // Mock GoogleApiAvailability to return SUCCESS
        mockkStatic(GoogleApiAvailability::class)
        every {
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        } returns ConnectionResult.SUCCESS

        // Mock Task and PaymentsClient
        val task = mockk<Task<Boolean>>()
        every { paymentsClient.isReadyToPay(any()) } returns task
        every { task.addOnCompleteListener(any()) } answers {
            // Simulate ApiException being thrown in the callback
            val listener = arg<OnCompleteListener<Boolean>>(0)
            val exception = ApiException(Status.RESULT_TIMEOUT) // Simulate a network error
            listener.onComplete(Tasks.forException(exception))
            task
        }

        // Invoke the method
        val result = googlePayFacade.checkIfIsReadyToPay(emptyList(), emptyList(), true, false)

        // Verify that the result is false
        assertFalse(result)
    }

    @Test
    fun `pay method calls AutoResolveHelper with the correct parameters`() {
        val activity = mockk<Activity>(relaxed = true)
        val paymentDataRequest = mockk<PaymentDataRequest>(relaxed = true)
        val task = mockk<Task<PaymentData>>(relaxed = true)

        mockkStatic(AutoResolveHelper::class)
        mockkStatic(PaymentDataRequest::class)

        val json = googlePayFacade.run {
            buildPaymentRequest(
                gatewayMerchantId = "test_merchant_id",
                merchantName = "Test Merchant",
                totalPrice = "10.00",
                countryCode = "US",
                currencyCode = "USD",
                allowedCardNetworks = listOf("VISA", "MASTERCARD"),
                allowedCardAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS"),
                billingAddressRequired = true,
                shippingOptions = null,
                shippingAddressParameters = null,
                requireShippingMethod = false,
                emailAddressRequired = false
            )
        }

        every { PaymentDataRequest.fromJson(json.toString()) } returns paymentDataRequest
        every { paymentsClient.loadPaymentData(paymentDataRequest) } returns task

        googlePayFacade.pay(
            activity = activity,
            gatewayMerchantId = "test_merchant_id",
            merchantName = "Test Merchant",
            totalPrice = "10.00",
            countryCode = "US",
            currencyCode = "USD",
            allowedCardNetworks = listOf("VISA", "MASTERCARD"),
            allowedCardAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS"),
            billingAddressRequired = true,
            shippingOptions = null,
            shippingAddressParameters = null,
            requireShippingMethod = false,
            emailAddressRequired = false
        )

        verify {
            AutoResolveHelper.resolveTask(
                task,
                activity,
                GooglePayFacade.GOOGLE_PAY_REQUEST_CODE
            )
        }

        unmockkStatic(AutoResolveHelper::class)
    }

    @Test
    fun `buildPaymentRequest creates a valid JSON request`() {
        val json = googlePayFacade.run {
            buildPaymentRequest(
                gatewayMerchantId = "test_merchant_id",
                merchantName = "Test Merchant",
                totalPrice = "10.00",
                countryCode = "US",
                currencyCode = "USD",
                allowedCardNetworks = listOf("VISA", "MASTERCARD"),
                allowedCardAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS"),
                billingAddressRequired = true,
                shippingOptions = CheckoutModule.Shipping(
                    selectedMethod = "STANDARD_SHIPPING",
                    shippingMethods = listOf(
                        ShippingMethod("STANDARD_SHIPPING", "Standard Shipping", 1, "1")
                    )
                ),
                shippingAddressParameters = PrimerGoogleShippingAddressParameters(phoneNumberRequired = true),
                requireShippingMethod = true,
                emailAddressRequired = true
            )
        }

        assertNotNull(json)
        assertEquals(2, json.getInt("apiVersion"))
        assertEquals(0, json.getInt("apiVersionMinor"))
        assertTrue(json.has("allowedPaymentMethods"))
        assertTrue(json.has("transactionInfo"))
        assertTrue(json.has("merchantInfo"))
        assertTrue(json.has("shippingAddressRequired"))
        assertEquals("Test Merchant", json.getJSONObject("merchantInfo").getString("merchantName"))
        assertEquals("FINAL", json.getJSONObject("transactionInfo").getString("totalPriceStatus"))

        val shippingOptionParameters = json.getJSONObject("shippingOptionParameters")
        assertEquals("STANDARD_SHIPPING", shippingOptionParameters.getString("defaultSelectedOptionId"))
        val shippingMethods = shippingOptionParameters.getJSONArray("shippingOptions")
        assertEquals(1, shippingMethods.length())
        val firstShippingMethod = shippingMethods.getJSONObject(0)
        assertEquals("STANDARD_SHIPPING", firstShippingMethod.getString("label"))
        assertEquals("Standard Shipping", firstShippingMethod.getString("description"))
        assertEquals("1", firstShippingMethod.getString("id"))

        assertTrue(json.getBoolean("shippingAddressRequired"))
        val shippingAddressParams = json.getJSONObject("shippingAddressParameters")
        assertTrue(shippingAddressParams.getBoolean("phoneNumberRequired"))
        assertTrue(json.getBoolean("shippingOptionRequired"))
        assertTrue(json.getBoolean("emailRequired"))
    }
}
