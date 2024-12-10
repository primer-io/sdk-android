package io.primer.cardShared.networks.data.repository

import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.cardShared.networks.domain.repository.OrderedAllowedCardNetworksRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OrderedAllowedCardNetworksDataRepositoryTest {

    private lateinit var repository: OrderedAllowedCardNetworksRepository
    private lateinit var configurationDataSource: CacheConfigurationDataSource

    @BeforeEach
    fun setUp() {
        configurationDataSource = mockk()
        repository = OrderedAllowedCardNetworksDataRepository(configurationDataSource)
    }

    @Test
    fun `getOrderedAllowedCardNetworks should return ordered allowed card networks from configuration data source`() {
        // Mock data
        val orderedCardNetworks = listOf(
            CardNetwork.Type.VISA,
            CardNetwork.Type.MASTERCARD,
            CardNetwork.Type.DISCOVER
        )
        val clientSession = mockk<ClientSessionDataResponse> {
            every { paymentMethod?.orderedAllowedCardNetworks } returns orderedCardNetworks
        }

        // Mock configuration data source behavior
        every { configurationDataSource.get().clientSession } returns clientSession

        // Call method under test
        val result = repository.getOrderedAllowedCardNetworks()

        // Verify
        assertEquals(orderedCardNetworks, result)
    }

    @Test
    fun `getOrderedAllowedCardNetworks should return empty list when payment method is null`() {
        // Mock data with null payment method
        val clientSession = mockk<ClientSessionDataResponse> {
            every { paymentMethod } returns null
        }

        // Mock configuration data source behavior
        every { configurationDataSource.get().clientSession } returns clientSession

        // Call method under test
        val result = repository.getOrderedAllowedCardNetworks()

        // Verify
        assertEquals(emptyList(), result)
    }

    @Test
    fun `getOrderedAllowedCardNetworks should return empty list when ordered allowed card networks is null`() {
        val clientSession = mockk<ClientSessionDataResponse> {
            every { paymentMethod?.orderedAllowedCardNetworks } returns emptyList()
        }

        // Mock configuration data source behavior
        every { configurationDataSource.get().clientSession } returns clientSession

        // Call method under test
        val result = repository.getOrderedAllowedCardNetworks()

        // Verify
        assertEquals(emptyList(), result)
    }
}
