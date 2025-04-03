package io.primer.android.configuration.data.extensions

import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.data.model.ClientSessionDataResponse.PaymentMethodDataResponse.Companion.PAYMENT_CARD_TYPE
import io.primer.android.configuration.domain.model.Surcharge
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PaymentMethodDataResponseTest {

    @Test
    fun `surcharges() returns correct mapping for card networks`() {
        val visaNetwork = mockk<ClientSessionDataResponse.NetworkOptionDataResponse> {
            every { type } returns CardNetwork.Type.VISA.name
            every { surcharge } returns 100
        }

        val mastercardNetwork = mockk<ClientSessionDataResponse.NetworkOptionDataResponse> {
            every { type } returns CardNetwork.Type.MASTERCARD.name
            every { surcharge } returns 200
        }

        val cardOption = mockk<ClientSessionDataResponse.PaymentMethodOptionDataResponse> {
            every { type } returns PAYMENT_CARD_TYPE
            every { networks } returns listOf(visaNetwork, mastercardNetwork)
            every { surcharge } returns null
        }

        val response = mockk<ClientSessionDataResponse.PaymentMethodDataResponse> {
            every { options } returns listOf(cardOption)
        }

        val expected = mapOf(
            PAYMENT_CARD_TYPE to Surcharge.CardNetworksSurcharge(
                mapOf(CardNetwork.Type.VISA.name to 100, CardNetwork.Type.MASTERCARD.name to 200),
            ),
        )

        assertEquals(expected, response.surcharges())
    }

    @Test
    fun `surcharges() returns correct mapping for non-card payment methods`() {
        val paymentMethodType = "PAYPAL"
        val paypalOption = mockk<ClientSessionDataResponse.PaymentMethodOptionDataResponse> {
            every { type } returns paymentMethodType
            every { networks } returns null
            every { surcharge } returns 300
        }

        val response = mockk<ClientSessionDataResponse.PaymentMethodDataResponse> {
            every { options } returns listOf(paypalOption)
        }

        val expected = mapOf(
            paymentMethodType to Surcharge.PaymentMethodSurcharge(300),
        )

        assertEquals(expected, response.surcharges())
    }

    @Test
    fun `surcharges() returns empty map when no options are available`() {
        val response = mockk<ClientSessionDataResponse.PaymentMethodDataResponse> {
            every { options } returns emptyList()
        }

        assertEquals(emptyMap<String, Surcharge>(), response.surcharges())
    }

    @Test
    fun `surcharges() handles missing surcharge values for non-card payments`() {
        val paymentMethodType = "PAYPAL"
        val applePayOption = mockk<ClientSessionDataResponse.PaymentMethodOptionDataResponse> {
            every { type } returns paymentMethodType
            every { networks } returns null
            every { surcharge } returns null
        }

        val response = mockk<ClientSessionDataResponse.PaymentMethodDataResponse> {
            every { options } returns listOf(applePayOption)
        }

        val expected = mapOf(
            paymentMethodType to Surcharge.PaymentMethodSurcharge(0),
        )

        assertEquals(expected, response.surcharges())
    }

    @Test
    fun `surcharges() returns empty map when networks list is null for card payments`() {
        val cardOption = mockk<ClientSessionDataResponse.PaymentMethodOptionDataResponse> {
            every { type } returns PAYMENT_CARD_TYPE
            every { networks } returns null
            every { surcharge } returns null
        }

        val response = mockk<ClientSessionDataResponse.PaymentMethodDataResponse> {
            every { options } returns listOf(cardOption)
        }

        val expected = mapOf(
            PAYMENT_CARD_TYPE to Surcharge.CardNetworksSurcharge(emptyMap()),
        )

        assertEquals(expected, response.surcharges())
    }
}
