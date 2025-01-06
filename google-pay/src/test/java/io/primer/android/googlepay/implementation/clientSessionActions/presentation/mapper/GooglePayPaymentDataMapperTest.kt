package io.primer.android.googlepay.implementation.clientSessionActions.presentation.mapper

import com.google.android.gms.wallet.PaymentData
import io.mockk.every
import io.mockk.mockk
import io.primer.android.clientSessionActions.domain.models.ActionUpdateBillingAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateEmailAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateMobileNumberParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingOptionIdParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GooglePayPaymentDataMapperTest {
    private val json = """{
    "email": "dragon@primer.io",
    "paymentMethodData": {
        "info": {
            "billingAddress": {
                "name": "John Doe",
                "address1": "123 Main St",
                "address2": "Apt 4B",
                "locality": "Anytown",
                "postalCode": "12345",
                "countryCode": "US",
                "administrativeArea": "CA"
            }
        }
    },
    "shippingOptionData": {
        "id": "EXPRESS"
    },
    "shippingAddress": {
        "phoneNumber": "1234567890",
        "name": "John Doe",
        "address1": "123 Main St",
        "address2": "Apt 4B",
        "locality": "Anytown",
        "postalCode": "12345",
        "countryCode": "US",
        "administrativeArea": "CA"
    }
}"""

    private val paymentData = mockk<PaymentData>()

    @BeforeEach
    fun setUp() {
        every { paymentData.toJson() } returns json
    }

    @Test
    fun `mapToClientSessionUpdateParams should map PaymentData to ActionUpdateBillingAddressParams`() {
        val json = """{
            "paymentMethodData": {
                "info": {
                    "billingAddress": {
                        "name": "John Doe",
                        "address1": "123 Main St",
                        "address2": "Apt 4B",
                        "locality": "Anytown",
                        "postalCode": "12345",
                        "countryCode": "US",
                        "administrativeArea": "CA"
                    }
                }
            }
        }"""
        every { paymentData.toJson() } returns json

        val expected =
            ActionUpdateBillingAddressParams(
                firstName = "John",
                lastName = "Doe",
                addressLine1 = "123 Main St",
                addressLine2 = "Apt 4B",
                city = "Anytown",
                postalCode = "12345",
                countryCode = "US",
                state = "CA",
            )
        assertEquals(expected, paymentData.mapToClientSessionUpdateParams())
    }

    @Test
    fun `mapToShippingOptionIdParams should map PaymentData to ActionUpdateShippingOptionIdParams`() {
        val expected = ActionUpdateShippingOptionIdParams("EXPRESS")
        assertEquals(expected, paymentData.mapToShippingOptionIdParams())
    }

    @Test
    fun `mapToShippingOptionIdParams should return null when PaymentData is null`() {
        val paymentData: PaymentData? = null
        val result = paymentData.mapToShippingOptionIdParams()
        assertNull(result)
    }

    @Test
    fun `mapToShippingOptionIdParams should return null when PaymentData does not contain shipping option ID`() {
        every { paymentData.toJson() } returns "{ }"
        val result = paymentData.mapToShippingOptionIdParams()
        assertNull(result)
    }

    @Test
    fun `mapToMobileNumberParams should return null when PaymentData is null`() {
        val paymentData: PaymentData? = null
        val result = paymentData.mapToMobileNumberParams()
        assertNull(result)
    }

    @Test
    fun `mapToMobileNumberParams should return null when PaymentData does not contain mobile number`() {
        every { paymentData.toJson() } returns "{ }"
        val result = paymentData.mapToMobileNumberParams()
        assertNull(result)
    }

    @Test
    fun `mapToMobileNumberParams should map PaymentData to ActionUpdateMobileNumberParams`() {
        val expected = ActionUpdateMobileNumberParams("1234567890")
        assertEquals(expected, paymentData.mapToMobileNumberParams())
    }

    @Test
    fun `mapToShippingAddressParams should return null when PaymentData is null`() {
        val paymentData: PaymentData? = null
        val result = paymentData.mapToShippingAddressParams()
        assertNull(result)
    }

    @Test
    fun `mapToShippingAddressParams should return null when PaymentData does not contain shipping address`() {
        every { paymentData.toJson() } returns "{}"
        val result = paymentData.mapToShippingAddressParams()
        assertNull(result)
    }

    @Test
    fun `mapToShippingAddressParams should map PaymentData to ActionUpdateShippingAddressParams`() {
        val expected =
            ActionUpdateShippingAddressParams(
                firstName = "John",
                lastName = "Doe",
                addressLine1 = "123 Main St",
                addressLine2 = "Apt 4B",
                addressLine3 = "",
                city = "Anytown",
                postalCode = "12345",
                countryCode = "US",
                state = "CA",
            )
        assertEquals(expected, paymentData.mapToShippingAddressParams())
    }

    @Test
    fun `mapToEmailAddressParams should return null when PaymentData is null`() {
        val paymentData: PaymentData? = null
        val result = paymentData.mapToEmailAddressParams()
        assertNull(result)
    }

    @Test
    fun `mapToEmailAddressParams should return null when PaymentData does not contain email`() {
        every { paymentData.toJson() } returns "{ }"
        val result = paymentData.mapToEmailAddressParams()
        assertNull(result)
    }

    @Test
    fun `mapToEmailAddressParams should return ActionUpdateEmailAddressParams when PaymentData contains email`() {
        val result = paymentData.mapToEmailAddressParams()
        assertEquals(ActionUpdateEmailAddressParams(email = "dragon@primer.io"), result)
    }

    @Test
    fun `mapToMultipleActionUpdateParams should correctly map PaymentData to MultipleActionUpdateParams`() {
        val expected =
            MultipleActionUpdateParams(
                listOf(
                    ActionUpdateBillingAddressParams(
                        firstName = "John",
                        lastName = "Doe",
                        addressLine1 = "123 Main St",
                        addressLine2 = "Apt 4B",
                        city = "Anytown",
                        postalCode = "12345",
                        countryCode = "US",
                        state = "CA",
                    ),
                    ActionUpdateMobileNumberParams("1234567890"),
                    ActionUpdateShippingAddressParams(
                        firstName = "John",
                        lastName = "Doe",
                        addressLine1 = "123 Main St",
                        addressLine2 = "Apt 4B",
                        addressLine3 = "",
                        city = "Anytown",
                        postalCode = "12345",
                        countryCode = "US",
                        state = "CA",
                    ),
                    ActionUpdateEmailAddressParams("dragon@primer.io"),
                ),
            )

        assertEquals(expected, paymentData.mapToMultipleActionUpdateParams())
    }
}
