package io.primer.android.clientSessionActions.data.models

import io.primer.android.clientSessionActions.domain.models.ActionUpdateBillingAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateCustomerDetailsParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateEmailAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateMobileNumberParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingAddressParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.configuration.data.model.AddressData
import io.primer.android.configuration.data.model.CountryCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClientSessionActionParamsMapperKtTest {
    @Test
    fun `toActionData() should return list with first name, last name and email address when ActionUpdateCustomerDetailsParams contains first name, last name and email address`() {
        val result =
            ActionUpdateCustomerDetailsParams(
                firstName = "John",
                lastName = "Doe",
                emailAddress = "john@doe.com",
            ).toActionData()

        assertEquals(
            listOf(
                ClientSessionActionsDataRequest.SetCustomerFirstName("John"),
                ClientSessionActionsDataRequest.SetCustomerLastName("Doe"),
                ClientSessionActionsDataRequest.SetEmailAddress("john@doe.com"),
            ),
            result,
        )
    }

    @Test
    fun `toActionData() should return list with first name and last name when ActionUpdateCustomerDetailsParams contains only first name and last name`() {
        val result =
            ActionUpdateCustomerDetailsParams(
                firstName = "John",
                lastName = "Doe",
                emailAddress = null,
            ).toActionData()

        assertEquals(
            listOf(
                ClientSessionActionsDataRequest.SetCustomerFirstName("John"),
                ClientSessionActionsDataRequest.SetCustomerLastName("Doe"),
            ),
            result,
        )
    }

    @Test
    fun `toActionData() should return list with first name when ActionUpdateCustomerDetailsParams contains only first name`() {
        val result =
            ActionUpdateCustomerDetailsParams(
                firstName = "John",
                lastName = null,
                emailAddress = null,
            ).toActionData()

        assertEquals(
            listOf(
                ClientSessionActionsDataRequest.SetCustomerFirstName("John"),
            ),
            result,
        )
    }

    @Test
    fun `toActionData() should return empty list when ActionUpdateCustomerDetailsParams contains nothing`() {
        val result =
            ActionUpdateCustomerDetailsParams(
                firstName = null,
                lastName = null,
                emailAddress = null,
            ).toActionData()

        assertEquals(emptyList<ActionUpdateCustomerDetailsParams>(), result)
    }

    @Test
    fun `toActionData() should return list with shipping address when ActionUpdateShippingAddressParams contains address`() {
        val result =
            ActionUpdateShippingAddressParams(
                firstName = "John",
                lastName = "Doe",
                addressLine1 = "123 Main St",
                addressLine2 = "Apt 4B",
                addressLine3 = null,
                postalCode = "12345",
                city = "Anytown",
                countryCode = "US",
            ).toActionData()

        assertEquals(
            listOf(
                ClientSessionActionsDataRequest.SetShippingAddress(
                    AddressData(
                        firstName = "John",
                        lastName = "Doe",
                        addressLine1 = "123 Main St",
                        addressLine2 = "Apt 4B",
                        addressLine3 = null,
                        postalCode = "12345",
                        city = "Anytown",
                        countryCode = CountryCode.US,
                    ),
                ),
            ),
            result,
        )
    }

    @Test
    fun `toActionData() should return list with mobile number when ActionUpdateMobileNumberParams contains mobile number`() {
        val result =
            ActionUpdateMobileNumberParams(
                mobileNumber = "1234567890",
            ).toActionData()

        assertEquals(
            listOf(
                ClientSessionActionsDataRequest.SetMobileNumber("1234567890"),
            ),
            result,
        )
    }

    @Test
    fun `toActionData should correctly map EmailAddressUpdateParams`() {
        val result = ActionUpdateEmailAddressParams("mymail@nop.com").toActionData()

        assertEquals(listOf(ClientSessionActionsDataRequest.SetEmailAddress("mymail@nop.com")), result)
    }

    @Test
    fun `toActionData should correctly map MultipleActionUpdateParams`() {
        // Arrange
        val params =
            MultipleActionUpdateParams(
                listOf(
                    ActionUpdateCustomerDetailsParams(
                        firstName = "John",
                        lastName = "Doe",
                        emailAddress = "john.doe@example.com",
                    ),
                    ActionUpdateBillingAddressParams(
                        firstName = "John",
                        lastName = "Doe",
                        addressLine1 = "123 Main St",
                        addressLine2 = "Apt 4B",
                        postalCode = "12345",
                        city = "Anytown",
                        countryCode = "US",
                    ),
                ),
            )

        val expectedActions =
            listOf(
                ClientSessionActionsDataRequest.SetCustomerFirstName("John"),
                ClientSessionActionsDataRequest.SetCustomerLastName("Doe"),
                ClientSessionActionsDataRequest.SetEmailAddress("john.doe@example.com"),
                ClientSessionActionsDataRequest.SetBillingAddress(
                    AddressData(
                        firstName = "John",
                        lastName = "Doe",
                        addressLine1 = "123 Main St",
                        addressLine2 = "Apt 4B",
                        postalCode = "12345",
                        city = "Anytown",
                        countryCode = CountryCode.US,
                    ),
                ),
            )

        // Act
        val result = params.toActionData()

        // Assert
        assertEquals(expectedActions, result)
    }
}
