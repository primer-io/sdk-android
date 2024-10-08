package io.primer.android.components.data.payments.repository

import io.mockk.every
import io.mockk.mockk
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.CheckoutModuleDataResponse
import io.primer.android.data.configuration.models.CheckoutModuleType
import io.primer.android.domain.session.models.CheckoutModule
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
        val cardInformationResponse = CheckoutModuleDataResponse(
            type = CheckoutModuleType.CARD_INFORMATION,
            requestUrl = null,
            options = mapOf("option1" to true),
            shippingOptions = null
        )
        every {
            localConfigurationDataSource.getConfiguration().checkoutModules
        } returns
            listOf(cardInformationResponse)

        val result = repository.getCardInformation()
        val expected = CheckoutModule.CardInformation(mapOf("option1" to true))

        assertEquals(expected, result)
    }

    @Test
    fun `getCardInformation returns null when no CardInformation module is present`() {
        every { localConfigurationDataSource.getConfiguration().checkoutModules } returns emptyList()

        val result = repository.getCardInformation()

        assertNull(result)
    }

    @Test
    fun `getBillingAddress returns correct BillingAddress module`() {
        val billingAddressResponse = CheckoutModuleDataResponse(
            type = CheckoutModuleType.BILLING_ADDRESS,
            requestUrl = null,
            options = mapOf("option2" to false),
            shippingOptions = null
        )
        every { localConfigurationDataSource.getConfiguration().checkoutModules } returns listOf(billingAddressResponse)

        val result = repository.getBillingAddress()
        val expected = CheckoutModule.BillingAddress(mapOf("option2" to false))

        assertEquals(expected, result)
    }

    @Test
    fun `getBillingAddress returns null when no BillingAddress module is present`() {
        every { localConfigurationDataSource.getConfiguration().checkoutModules } returns emptyList()

        val result = repository.getBillingAddress()

        assertNull(result)
    }
}
