package io.primer.android.vouchers.retailOutlets.implementation.rpc.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.datasource.LocalRetailOutletDataSource
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.datasource.RemoteRetailOutletDataSource
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.models.RetailOutletDataRequest
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.models.RetailOutletDataResponse
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.models.RetailOutletResultDataResponse
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.repository.RetailOutletRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RetailOutletDataRepositoryTest {

    private val remoteDataSource: RemoteRetailOutletDataSource = mockk()
    private val localDataSource: LocalRetailOutletDataSource = mockk(relaxed = true)
    private val configurationDataSource: CacheConfigurationDataSource = mockk()
    private lateinit var repository: RetailOutletRepository

    @BeforeEach
    fun setUp() {
        repository = RetailOutletDataRepository(remoteDataSource, localDataSource, configurationDataSource)
    }

    @Test
    fun `getRetailOutlets should return outlets from remote source and update local cache`() = runTest {
        val paymentMethodConfigId = "config_id_123"
        val configuration = mockk<ConfigurationData> {
            every { coreUrl } returns "https://example.com"
        }
        val response = RetailOutletResultDataResponse(
            listOf(
                RetailOutletDataResponse("outlet_id_123", "Test Outlet", false, "https://example.com/icon.png"),
                RetailOutletDataResponse("outlet_id_456", "Another Outlet", true, "https://example.com/icon2.png")
            )
        )

        every { configurationDataSource.get() } returns configuration
        coEvery { remoteDataSource.execute(any<BaseRemoteHostRequest<RetailOutletDataRequest>>()) } returns response

        val outlets = repository.getRetailOutlets(paymentMethodConfigId).getOrNull()

        assertEquals(2, outlets?.size)
        assertEquals("outlet_id_123", outlets?.get(0)?.id)
        assertEquals("outlet_id_456", outlets?.get(1)?.id)

        coVerify { localDataSource.update(response.result) }
    }

    @Test
    fun `getCachedRetailOutlets should return outlets from local cache`() {
        val cachedData = listOf(
            RetailOutletDataResponse("outlet_id_123", "Test Outlet", false, "https://example.com/icon.png"),
            RetailOutletDataResponse("outlet_id_456", "Another Outlet", true, "https://example.com/icon2.png")
        )

        every { localDataSource.get() } returns cachedData.toMutableList()

        val cachedOutlets = repository.getCachedRetailOutlets()
        assertEquals(2, cachedOutlets.size)
        assertEquals("outlet_id_123", cachedOutlets[0].id)
        assertEquals("outlet_id_456", cachedOutlets[1].id)
    }
}
