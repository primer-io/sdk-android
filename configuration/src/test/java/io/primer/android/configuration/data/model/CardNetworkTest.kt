package io.primer.android.configuration.data.model

import io.primer.android.configuration.data.model.CardNetwork.Type
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class CardNetworkTest {

    @Test
    fun `lookupByCardNetwork returns correct cvvLabel for each registered network`() {
        assertLabel(Type.VISA, "CVV")
        assertLabel(Type.MASTERCARD, "CVC")
        assertLabel(Type.AMEX, "CID")
        assertLabel(Type.DINERS_CLUB, "CVV")
        assertLabel(Type.DISCOVER, "CID")
        assertLabel(Type.JCB, "CVV")
        assertLabel(Type.UNIONPAY, "CVN")
        assertLabel(Type.MAESTRO, "CVC")
        assertLabel(Type.ELO, "CVE")
        assertLabel(Type.MIR, "CVP2")
        assertLabel(Type.HIPER, "CVC")
        assertLabel(Type.HIPERCARD, "CVC")
        assertLabel(Type.DANKORT, "CVV")
    }

    @Test
    fun `lookupByCardNetwork falls back to CVV for unknown network name`() {
        assertEquals("CVV", CardNetwork.lookupByCardNetwork("UNKNOWN_FOO").cvvLabel)
        assertEquals("CVV", CardNetwork.lookupByCardNetwork("OTHER").cvvLabel)
    }

    @Test
    fun `Amex preserves cvvLength alongside new cvvLabel`() {
        val amex = CardNetwork.lookupByCardNetwork(Type.AMEX.name)
        assertEquals(4, amex.cvvLength)
        assertEquals("CID", amex.cvvLabel)
    }

    @Test
    fun `lookup by BIN resolves the correct cvvLabel`() {
        assertEquals("CVV", CardNetwork.lookup("4111").cvvLabel)
        assertEquals("CID", CardNetwork.lookup("3782").cvvLabel)
        assertEquals("CVC", CardNetwork.lookup("5555").cvvLabel)
    }

    private fun assertLabel(type: Type, expected: String) {
        assertEquals(
            expected,
            CardNetwork.lookupByCardNetwork(type.name).cvvLabel,
            "Unexpected cvvLabel for ${type.name}",
        )
    }
}
