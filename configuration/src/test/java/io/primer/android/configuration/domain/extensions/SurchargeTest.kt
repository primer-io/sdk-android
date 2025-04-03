package io.primer.android.configuration.domain.extensions

import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.domain.model.Surcharge
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SurchargeTest {

    @Test
    fun `disabled should return true for PaymentMethodSurcharge when amount is 0`() {
        val surcharge = Surcharge.PaymentMethodSurcharge(amount = 0)
        assertTrue(surcharge.disabled())
    }

    @Test
    fun `disabled should return false for PaymentMethodSurcharge when amount is greater than 0`() {
        val surcharge = Surcharge.PaymentMethodSurcharge(amount = 10)
        assertFalse(surcharge.disabled())
    }

    @Test
    fun `disabled should return true for CardNetworksSurcharge when all networks have a 0 surcharge`() {
        val surcharge = Surcharge.CardNetworksSurcharge(
            surcharges = mapOf(CardNetwork.Type.VISA.name to 0, CardNetwork.Type.MASTERCARD.name to 0),
        )
        assertTrue(surcharge.disabled())
    }

    @Test
    fun `disabled should return false for CardNetworksSurcharge when any network has a 0 surcharge`() {
        val surcharge = Surcharge.CardNetworksSurcharge(
            surcharges = mapOf(CardNetwork.Type.VISA.name to 5, CardNetwork.Type.MASTERCARD.name to 0),
        )
        assertFalse(surcharge.disabled())
    }

    @Test
    fun `disabled should return false for CardNetworksSurcharge when all surcharges are greater than 0`() {
        val surcharge = Surcharge.CardNetworksSurcharge(
            surcharges = mapOf(CardNetwork.Type.VISA.name to 5, CardNetwork.Type.MASTERCARD.name to 10),
        )
        assertFalse(surcharge.disabled())
    }
}
