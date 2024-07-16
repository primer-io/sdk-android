package io.primer.android.data.action.models

import io.primer.android.domain.action.models.ActionUpdateCustomerDetailsParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClientSessionActionsDataRequestKtTest {
    @Test
    fun `toActionData() should return list with first name, last name and email address when ActionUpdateCustomerDetailsParams contains first name, last name and email address`() {
        val result = ActionUpdateCustomerDetailsParams(
            firstName = "John",
            lastName = "Doe",
            emailAddress = "john@doe.com"
        ).toActionData()

        assertEquals(
            listOf(
                ClientSessionActionsDataRequest.SetCustomerFirstName(
                    ClientSessionActionsDataRequest.SetCustomerFirstNameRequestDataParams("John")
                ),
                ClientSessionActionsDataRequest.SetCustomerLastName(
                    ClientSessionActionsDataRequest.SetCustomerLastNameRequestDataParams("Doe")
                ),
                ClientSessionActionsDataRequest.SetEmailAddress(
                    ClientSessionActionsDataRequest.SetEmailAddressRequestDataParams("john@doe.com")
                )
            ),
            result
        )
    }

    @Test
    fun `toActionData() should return list with first name and last name when ActionUpdateCustomerDetailsParams contains only first name and last name`() {
        val result = ActionUpdateCustomerDetailsParams(
            firstName = "John",
            lastName = "Doe",
            emailAddress = null
        ).toActionData()

        assertEquals(
            listOf(
                ClientSessionActionsDataRequest.SetCustomerFirstName(
                    ClientSessionActionsDataRequest.SetCustomerFirstNameRequestDataParams("John")
                ),
                ClientSessionActionsDataRequest.SetCustomerLastName(
                    ClientSessionActionsDataRequest.SetCustomerLastNameRequestDataParams("Doe")
                )
            ),
            result
        )
    }

    @Test
    fun `toActionData() should return list with first name when ActionUpdateCustomerDetailsParams contains only first name`() {
        val result = ActionUpdateCustomerDetailsParams(
            firstName = "John",
            lastName = null,
            emailAddress = null
        ).toActionData()

        assertEquals(
            listOf(
                ClientSessionActionsDataRequest.SetCustomerFirstName(
                    ClientSessionActionsDataRequest.SetCustomerFirstNameRequestDataParams("John")
                )
            ),
            result
        )
    }

    @Test
    fun `toActionData() should return empty list when ActionUpdateCustomerDetailsParams contains nothing`() {
        val result = ActionUpdateCustomerDetailsParams(
            firstName = null,
            lastName = null,
            emailAddress = null
        ).toActionData()

        assertEquals(emptyList<ActionUpdateCustomerDetailsParams>(), result)
    }
}
