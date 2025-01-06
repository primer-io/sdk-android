package io.primer.android.configuration.data.repository

import io.mockk.every
import io.mockk.mockk
import io.primer.android.checkoutModules.data.repository.CheckoutModuleDataRepository
import io.primer.android.configuration.data.datasource.LocalConfigurationDataSource
import io.primer.android.configuration.data.model.CheckoutModuleDataResponse
import io.primer.android.configuration.data.model.CheckoutModuleType
import io.primer.android.configuration.domain.model.CheckoutModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CheckoutModuleDataRepositoryTest {
    private lateinit var repository: CheckoutModuleDataRepository
    private val localConfigurationDataSource: LocalConfigurationDataSource = mockk()

    @BeforeEach
    fun setUp() {
        repository = CheckoutModuleDataRepository(localConfigurationDataSource)
    }

    @Test
    fun `getCardInformation returns correct CardInformation module`() {
        val cardInformationResponse =
            CheckoutModuleDataResponse(
                type = CheckoutModuleType.CARD_INFORMATION,
                requestUrl = null,
                options = mapOf("option1" to true),
                shippingOptions = null,
            )
        every {
            localConfigurationDataSource.get().checkoutModules
        } returns listOf(cardInformationResponse)

        val result = repository.getCardInformation()
        val expected = CheckoutModule.CardInformation(mapOf("option1" to true))

        assertEquals(expected, result)
    }

    @Test
    fun `getCardInformation returns null when no CardInformation module is present`() {
        every { localConfigurationDataSource.get().checkoutModules } returns emptyList()

        val result = repository.getCardInformation()

        assertNull(result)
    }

    @Test
    fun `getBillingAddress returns correct BillingAddress module`() {
        val billingAddressResponse =
            CheckoutModuleDataResponse(
                type = CheckoutModuleType.BILLING_ADDRESS,
                requestUrl = null,
                options = mapOf("option2" to false),
                shippingOptions = null,
            )
        every { localConfigurationDataSource.get().checkoutModules } returns listOf(billingAddressResponse)

        val result = repository.getBillingAddress()
        val expected = CheckoutModule.BillingAddress(mapOf("option2" to false))

        assertEquals(expected, result)
    }

    @Test
    fun `getBillingAddress returns null when no BillingAddress module is present`() {
        every { localConfigurationDataSource.get().checkoutModules } returns emptyList()

        val result = repository.getBillingAddress()

        assertNull(result)
    }
}
