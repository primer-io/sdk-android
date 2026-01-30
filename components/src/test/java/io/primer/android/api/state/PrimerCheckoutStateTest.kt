package io.primer.android.api.state

import io.mockk.mockk
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PrimerCheckoutStateTest {

    @Test
    fun `Loading state is a singleton data object`() {
        val state1 = PrimerCheckoutState.Loading
        val state2 = PrimerCheckoutState.Loading
        assertEquals(state1, state2)
        assertTrue(state1 === state2)
    }

    @Test
    fun `Ready state contains client session`() {
        val mockSession = mockk<PrimerClientSession>(relaxed = true)
        val state = PrimerCheckoutState.Ready(mockSession)
        assertEquals(mockSession, state.clientSession)
    }

    @Test
    fun `Ready states with same session are equal`() {
        val mockSession = mockk<PrimerClientSession>(relaxed = true)
        val state1 = PrimerCheckoutState.Ready(mockSession)
        val state2 = PrimerCheckoutState.Ready(mockSession)
        assertEquals(state1, state2)
    }

    @Test
    fun `Ready states with different sessions are not equal`() {
        val mockSession1 = mockk<PrimerClientSession>(relaxed = true)
        val mockSession2 = mockk<PrimerClientSession>(relaxed = true)
        val state1 = PrimerCheckoutState.Ready(mockSession1)
        val state2 = PrimerCheckoutState.Ready(mockSession2)
        assertNotEquals(state1, state2)
    }

    @Test
    fun `Success state contains checkout data`() {
        val mockCheckoutData = mockk<PrimerCheckoutData>(relaxed = true)
        val state = PrimerCheckoutState.Success(mockCheckoutData)
        assertEquals(mockCheckoutData, state.checkoutData)
    }

    @Test
    fun `Success states with same checkout data are equal`() {
        val mockCheckoutData = mockk<PrimerCheckoutData>(relaxed = true)
        val state1 = PrimerCheckoutState.Success(mockCheckoutData)
        val state2 = PrimerCheckoutState.Success(mockCheckoutData)
        assertEquals(state1, state2)
    }

    @Test
    fun `Failure state contains error`() {
        val mockError = mockk<PrimerError>(relaxed = true)
        val state = PrimerCheckoutState.Failure(mockError)
        assertEquals(mockError, state.error)
    }

    @Test
    fun `Failure states with same error are equal`() {
        val mockError = mockk<PrimerError>(relaxed = true)
        val state1 = PrimerCheckoutState.Failure(mockError)
        val state2 = PrimerCheckoutState.Failure(mockError)
        assertEquals(state1, state2)
    }

    @Test
    fun `Cancelled state is a singleton data object`() {
        val state1 = PrimerCheckoutState.Cancelled
        val state2 = PrimerCheckoutState.Cancelled
        assertEquals(state1, state2)
        assertTrue(state1 === state2)
    }

    @Test
    fun `TokenCreated state contains token and payment method type`() {
        val token = "tok_test_123"
        val paymentMethodType = "PAYMENT_CARD"
        val state = PrimerCheckoutState.TokenCreated(token, paymentMethodType)
        assertEquals(token, state.token)
        assertEquals(paymentMethodType, state.paymentMethodType)
    }

    @Test
    fun `TokenCreated states with same values are equal`() {
        val state1 = PrimerCheckoutState.TokenCreated("tok_123", "PAYMENT_CARD")
        val state2 = PrimerCheckoutState.TokenCreated("tok_123", "PAYMENT_CARD")
        assertEquals(state1, state2)
    }

    @Test
    fun `TokenCreated states with different values are not equal`() {
        val state1 = PrimerCheckoutState.TokenCreated("tok_123", "PAYMENT_CARD")
        val state2 = PrimerCheckoutState.TokenCreated("tok_456", "PAYPAL")
        assertNotEquals(state1, state2)
    }

    @Test
    fun `different state types are not equal`() {
        val loading = PrimerCheckoutState.Loading
        val ready = PrimerCheckoutState.Ready(mockk(relaxed = true))
        val success = PrimerCheckoutState.Success(mockk(relaxed = true))
        val failure = PrimerCheckoutState.Failure(mockk(relaxed = true))
        val cancelled = PrimerCheckoutState.Cancelled
        val tokenCreated = PrimerCheckoutState.TokenCreated("tok", "CARD")

        assertNotEquals(loading, ready)
        assertNotEquals(ready, success)
        assertNotEquals(success, failure)
        assertNotEquals(failure, cancelled)
        assertNotEquals(cancelled, tokenCreated)
        assertNotEquals(tokenCreated, loading)
    }

    @Test
    fun `state types can be distinguished with when expression`() {
        val states: List<PrimerCheckoutState> = listOf(
            PrimerCheckoutState.Loading,
            PrimerCheckoutState.Ready(mockk(relaxed = true)),
            PrimerCheckoutState.Success(mockk(relaxed = true)),
            PrimerCheckoutState.Failure(mockk(relaxed = true)),
            PrimerCheckoutState.Cancelled,
            PrimerCheckoutState.TokenCreated("tok", "CARD"),
        )

        val types = states.map { state ->
            when (state) {
                is PrimerCheckoutState.Loading -> "Loading"
                is PrimerCheckoutState.Ready -> "Ready"
                is PrimerCheckoutState.Success -> "Success"
                is PrimerCheckoutState.Failure -> "Failure"
                is PrimerCheckoutState.Cancelled -> "Cancelled"
                is PrimerCheckoutState.TokenCreated -> "TokenCreated"
            }
        }

        assertEquals(
            listOf("Loading", "Ready", "Success", "Failure", "Cancelled", "TokenCreated"),
            types,
        )
    }
}
