package io.primer.statetransport.data.model

import io.primer.statetransport.domain.model.CheckoutOutcome
import io.primer.statetransport.domain.model.ClientInstructions
import io.primer.statetransport.domain.model.PaymentStatus
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ClientInstructionDataResponseTest {

    @Test
    fun `deserializer should parse WAIT instruction`() {
        val json = JSONObject().apply {
            put("type", "WAIT")
            put("pollDelayMilliseconds", 5000L)
        }

        val result = ClientInstructionDataResponse.deserializer.deserialize(json)

        assertEquals(ClientInstructionType.WAIT, result.type)
        assertEquals(5000L, result.pollDelayMilliseconds)
        assertNull(result.payload)
    }

    @Test
    fun `deserializer should parse EXECUTE instruction with payload`() {
        val payload = JSONObject().apply { put("key", "value") }
        val json = JSONObject().apply {
            put("type", "EXECUTE")
            put("pollDelayMilliseconds", 3000L)
            put("payload", payload)
        }

        val result = ClientInstructionDataResponse.deserializer.deserialize(json)

        assertEquals(ClientInstructionType.EXECUTE, result.type)
        assertEquals(3000L, result.pollDelayMilliseconds)
        assertEquals(payload.toString(), result.payload)
    }

    @Test
    fun `deserializer should parse END instruction`() {
        val json = JSONObject().apply {
            put("type", "END")
            put("pollDelayMilliseconds", 0L)
        }

        val result = ClientInstructionDataResponse.deserializer.deserialize(json)

        assertEquals(ClientInstructionType.END, result.type)
    }

    @Test
    fun `deserializer should default pollDelayMilliseconds to 0 when missing`() {
        val json = JSONObject().apply {
            put("type", "WAIT")
        }

        val result = ClientInstructionDataResponse.deserializer.deserialize(json)

        assertEquals(0L, result.pollDelayMilliseconds)
    }

    @Test
    fun `deserializer should parse ClientSessionInstructionResponse`() {
        val json = JSONObject().apply {
            put(
                "clientInstruction",
                JSONObject().apply {
                    put("type", "WAIT")
                    put("pollDelayMilliseconds", 1000L)
                },
            )
        }

        val result = ClientSessionInstructionResponse.deserializer.deserialize(json)

        assertEquals(ClientInstructionType.WAIT, result.clientInstruction.type)
        assertEquals(1000L, result.clientInstruction.pollDelayMilliseconds)
    }

    @Test
    fun `deserializer should parse envelope with approvalMode and full currentAttempt`() {
        val json = JSONObject().apply {
            put(
                "clientInstruction",
                JSONObject().apply {
                    put("type", "EXECUTE")
                    put("pollDelayMilliseconds", 1000L)
                    put("payload", JSONObject().apply { put("key", "value") })
                },
            )
            put(
                "currentAttempt",
                JSONObject().apply {
                    put("id", "attempt_1")
                    put("paymentInstrumentTokenId", "token_1")
                    put("paymentId", "pay_1")
                },
            )
        }

        val result = ClientSessionInstructionResponse.deserializer.deserialize(json)

        assertEquals("attempt_1", result.currentAttempt?.id)
        assertEquals("token_1", result.currentAttempt?.paymentInstrumentTokenId)
        assertEquals("pay_1", result.currentAttempt?.paymentId)
    }

    @Test
    fun `deserializer should keep envelope on WAIT responses`() {
        val json = JSONObject().apply {
            put(
                "clientInstruction",
                JSONObject().apply {
                    put("type", "WAIT")
                    put("pollDelayMilliseconds", 2000L)
                },
            )
            put(
                "currentAttempt",
                JSONObject().apply {
                    put("id", "attempt_wait")
                },
            )
        }

        val result = ClientSessionInstructionResponse.deserializer.deserialize(json)

        assertEquals(ClientInstructionType.WAIT, result.clientInstruction.type)
        assertEquals("attempt_wait", result.currentAttempt?.id)
    }

    @Test
    fun `deserializer should keep envelope on END responses`() {
        val json = JSONObject().apply {
            put(
                "clientInstruction",
                JSONObject().apply {
                    put("type", "END")
                    put("pollDelayMilliseconds", 0L)
                },
            )
            put(
                "currentAttempt",
                JSONObject().apply {
                    put("id", "attempt_end")
                },
            )
        }

        val result = ClientSessionInstructionResponse.deserializer.deserialize(json)

        assertEquals(ClientInstructionType.END, result.clientInstruction.type)
        assertEquals("attempt_end", result.currentAttempt?.id)
    }

    @Test
    fun `deserializer should return null envelope fields when absent`() {
        val json = JSONObject().apply {
            put(
                "clientInstruction",
                JSONObject().apply {
                    put("type", "WAIT")
                    put("pollDelayMilliseconds", 1000L)
                },
            )
        }

        val result = ClientSessionInstructionResponse.deserializer.deserialize(json)

        assertNull(result.currentAttempt)
    }

    @Test
    fun `deserializer should parse currentAttempt with only id`() {
        val json = JSONObject().apply {
            put(
                "clientInstruction",
                JSONObject().apply {
                    put("type", "WAIT")
                    put("pollDelayMilliseconds", 1000L)
                },
            )
            put(
                "currentAttempt",
                JSONObject().apply {
                    put("id", "attempt_only_id")
                },
            )
        }

        val result = ClientSessionInstructionResponse.deserializer.deserialize(json)

        assertEquals("attempt_only_id", result.currentAttempt?.id)
        assertNull(result.currentAttempt?.paymentInstrumentTokenId)
        assertNull(result.currentAttempt?.paymentId)
    }

    // toInstructions() tests

    @Test
    fun `toInstructions should return Wait when response is null`() {
        val result = (null as ClientInstructionDataResponse?).toInstructions()

        assertTrue(result is ClientInstructions.Wait)
        assertEquals(0L, (result as ClientInstructions.Wait).pollDelayMilliseconds)
    }

    @Test
    fun `toInstructions should return Wait for WAIT type`() {
        val response = ClientInstructionDataResponse(
            type = ClientInstructionType.WAIT,
            pollDelayMilliseconds = 2000L,
            payload = null,
        )

        val result = response.toInstructions()

        assertTrue(result is ClientInstructions.Wait)
        assertEquals(2000L, (result as ClientInstructions.Wait).pollDelayMilliseconds)
    }

    @Test
    fun `toInstructions should return Execute for EXECUTE type`() {
        val response = ClientInstructionDataResponse(
            type = ClientInstructionType.EXECUTE,
            pollDelayMilliseconds = 1500L,
            payload = """{"action":"tokenize"}""",
        )

        val result = response.toInstructions()

        assertTrue(result is ClientInstructions.Execute)
        val execute = result as ClientInstructions.Execute
        assertEquals(1500L, execute.pollDelayMilliseconds)
        assertEquals("""{"action":"tokenize"}""", execute.payload)
    }

    @Test
    fun `toInstructions should return End with checkoutOutcome and payment for END type`() {
        val payload = JSONObject().apply {
            put("checkoutOutcome", "CHECKOUT_COMPLETE")
            put(
                "payment",
                JSONObject().apply {
                    put("id", "pay_123")
                    put("date", "2026-04-06")
                    put("amount", 1000L)
                    put("currencyCode", "USD")
                    put("status", "SUCCESS")
                    put("customerId", "cust_456")
                    put("orderId", "order_789")
                },
            )
        }
        val response = ClientInstructionDataResponse(
            type = ClientInstructionType.END,
            pollDelayMilliseconds = 0L,
            payload = payload.toString(),
        )

        val result = response.toInstructions()

        assertTrue(result is ClientInstructions.End)
        val end = result as ClientInstructions.End
        assertEquals(CheckoutOutcome.CHECKOUT_COMPLETE, end.checkoutOutcome)
        assertEquals("pay_123", end.payment?.id)
        assertEquals("2026-04-06", end.payment?.date)
        assertEquals(1000L, end.payment?.amount)
        assertEquals("USD", end.payment?.currencyCode)
        assertEquals(PaymentStatus.SUCCESS, end.payment?.status)
        assertEquals("cust_456", end.payment?.customerId)
        assertEquals("order_789", end.payment?.orderId)
    }

    @Test
    fun `toInstructions should return End with null checkoutOutcome when field is missing`() {
        val payload = JSONObject().apply {
            put(
                "payment",
                JSONObject().apply {
                    put("id", "pay_123")
                    put("date", "2026-04-06")
                    put("amount", 500L)
                    put("currencyCode", "EUR")
                    put("status", "PENDING")
                    put("orderId", "order_1")
                },
            )
        }
        val response = ClientInstructionDataResponse(
            type = ClientInstructionType.END,
            pollDelayMilliseconds = 0L,
            payload = payload.toString(),
        )

        val result = response.toInstructions() as ClientInstructions.End

        assertNull(result.checkoutOutcome)
    }

    @Test
    fun `toInstructions should return End with null payment when payment field is missing`() {
        val payload = JSONObject().apply {
            put("checkoutOutcome", "CHECKOUT_FAILURE")
        }
        val response = ClientInstructionDataResponse(
            type = ClientInstructionType.END,
            pollDelayMilliseconds = 0L,
            payload = payload.toString(),
        )

        val result = response.toInstructions() as ClientInstructions.End

        assertEquals(CheckoutOutcome.CHECKOUT_FAILURE, result.checkoutOutcome)
        assertNull(result.payment)
    }

    @Test
    fun `toInstructions should return End with null customerId when not present`() {
        val payload = JSONObject().apply {
            put("checkoutOutcome", "CHECKOUT_COMPLETE")
            put(
                "payment",
                JSONObject().apply {
                    put("id", "pay_1")
                    put("date", "2026-04-06")
                    put("amount", 100L)
                    put("currencyCode", "GBP")
                    put("status", "SUCCESS")
                    put("orderId", "order_1")
                },
            )
        }
        val response = ClientInstructionDataResponse(
            type = ClientInstructionType.END,
            pollDelayMilliseconds = 0L,
            payload = payload.toString(),
        )

        val result = response.toInstructions() as ClientInstructions.End

        assertNull(result.payment?.customerId)
    }

    @Test
    fun `toInstructions should parse DETERMINE_FROM_PAYMENT_STATUS outcome`() {
        val payload = JSONObject().apply {
            put("checkoutOutcome", "DETERMINE_FROM_PAYMENT_STATUS")
        }
        val response = ClientInstructionDataResponse(
            type = ClientInstructionType.END,
            pollDelayMilliseconds = 0L,
            payload = payload.toString(),
        )

        val result = response.toInstructions() as ClientInstructions.End

        assertEquals(CheckoutOutcome.DETERMINE_FROM_PAYMENT_STATUS, result.checkoutOutcome)
    }

    @Test
    fun `toInstructions should throw for unknown checkoutOutcome value`() {
        val payload = JSONObject().apply {
            put("checkoutOutcome", "UNKNOWN_VALUE")
        }
        val response = ClientInstructionDataResponse(
            type = ClientInstructionType.END,
            pollDelayMilliseconds = 0L,
            payload = payload.toString(),
        )

        assertThrows<IllegalArgumentException> {
            response.toInstructions()
        }
    }

    // toInstructionFetch() tests

    private fun waitResponse(
        currentAttempt: CurrentAttemptDataResponse? = null,
    ) = ClientSessionInstructionResponse(
        clientInstruction = ClientInstructionDataResponse(
            type = ClientInstructionType.WAIT,
            pollDelayMilliseconds = 1000L,
            payload = null,
        ),
        currentAttempt = currentAttempt,
    )

    @Test
    fun `toInstructionFetch should map full currentAttempt`() {
        val response = waitResponse(
            currentAttempt = CurrentAttemptDataResponse(
                id = "attempt_1",
                paymentInstrumentTokenId = "token_1",
                paymentId = "pay_1",
            ),
        )

        val result = response.toInstructionFetch()

        assertEquals("attempt_1", result.currentAttempt?.id)
        assertEquals("token_1", result.currentAttempt?.paymentInstrumentTokenId)
        assertEquals("pay_1", result.currentAttempt?.paymentId)
    }

    @Test
    fun `toInstructionFetch should map currentAttempt with only id`() {
        val response = waitResponse(
            currentAttempt = CurrentAttemptDataResponse(
                id = "attempt_only_id",
                paymentInstrumentTokenId = null,
                paymentId = null,
            ),
        )

        val result = response.toInstructionFetch()

        assertEquals("attempt_only_id", result.currentAttempt?.id)
        assertNull(result.currentAttempt?.paymentInstrumentTokenId)
        assertNull(result.currentAttempt?.paymentId)
    }

    @Test
    fun `toInstructionFetch should return null envelope fields when absent`() {
        val result = waitResponse().toInstructionFetch()

        assertNull(result.currentAttempt)
    }
}
