package io.primer.android.domain.session.models

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.data.configuration.models.CheckoutModuleDataResponse
import io.primer.android.data.configuration.models.CheckoutModuleType
import io.primer.android.data.configuration.models.ShippingMethod
import io.primer.android.data.configuration.models.ShippingOptions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConfigurationKtTest {

    @Test
    fun `toCheckoutModule converts BILLING_ADDRESS type correctly`() {
        val response = CheckoutModuleDataResponse(
            type = CheckoutModuleType.BILLING_ADDRESS,
            requestUrl = null,
            options = mapOf("option1" to true),
            shippingOptions = null
        )
        val module = response.toCheckoutModule()
        assertEquals(CheckoutModule.BillingAddress(mapOf("option1" to true)), module)
    }

    @Test
    fun `toCheckoutModule converts CARD_INFORMATION type correctly`() {
        val response = CheckoutModuleDataResponse(
            type = CheckoutModuleType.CARD_INFORMATION,
            requestUrl = null,
            options = mapOf("option2" to false),
            shippingOptions = null
        )
        val module = response.toCheckoutModule()
        assertEquals(CheckoutModule.CardInformation(mapOf("option2" to false)), module)
    }

    @Test
    fun `toCheckoutModule converts SHIPPING type correctly`() {
        val shippingMethods = listOf(
            ShippingMethod("name1", "description1", 100, "id1"),
            ShippingMethod("name2", "description2", 200, "id2")
        )
        val shippingOptions = ShippingOptions(shippingMethods, "id1")
        val response = CheckoutModuleDataResponse(
            type = CheckoutModuleType.SHIPPING,
            requestUrl = null,
            options = null,
            shippingOptions = shippingOptions
        )
        val module = response.toCheckoutModule()
        assertEquals(CheckoutModule.Shipping(shippingMethods, "id1"), module)
    }

    @Test
    fun `toCheckoutModule converts UNKNOWN type correctly`() {
        val response = CheckoutModuleDataResponse(
            type = CheckoutModuleType.UNKNOWN,
            requestUrl = null,
            options = mapOf("option3" to true),
            shippingOptions = null
        )
        val module = response.toCheckoutModule()
        assertEquals(CheckoutModule.Unknown, module)
    }

    @Test
    fun `isCardHolderNameEnabledOrNull returns true when options are null`() {
        val cardInformation = CheckoutModule.CardInformation(null)
        assertTrue(cardInformation.isCardHolderNameEnabled())
    }

    @Test
    fun `isCardHolderNameEnabledOrNull returns true when options are empty`() {
        val cardInformation = CheckoutModule.CardInformation(emptyMap())
        assertTrue(cardInformation.isCardHolderNameEnabled())
    }

    @Test
    fun `isCardHolderNameEnabledOrNull returns true when ALL is enabled`() {
        val options = mapOf(PrimerInputElementType.ALL.field to true)
        val cardInformation = CheckoutModule.CardInformation(options)
        assertTrue(cardInformation.isCardHolderNameEnabled())
    }

    @Test
    fun `isCardHolderNameEnabledOrNull returns false when ALL is disabled`() {
        val options = mapOf(PrimerInputElementType.ALL.field to false)
        val cardInformation = CheckoutModule.CardInformation(options)
        assertFalse(cardInformation.isCardHolderNameEnabled())
    }

    @Test
    fun `isCardHolderNameEnabledOrNull returns true when CARDHOLDER_NAME is enabled`() {
        val options = mapOf(PrimerInputElementType.CARDHOLDER_NAME.field to true)
        val cardInformation = CheckoutModule.CardInformation(options)
        assertTrue(cardInformation.isCardHolderNameEnabled())
    }

    @Test
    fun `isCardHolderNameEnabledOrNull returns false when CARDHOLDER_NAME is disabled`() {
        val options = mapOf(PrimerInputElementType.CARDHOLDER_NAME.field to false)
        val cardInformation = CheckoutModule.CardInformation(options)
        assertFalse(cardInformation.isCardHolderNameEnabled())
    }
}
