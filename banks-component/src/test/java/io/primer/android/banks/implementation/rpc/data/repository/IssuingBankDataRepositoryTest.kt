package io.primer.android.banks.implementation.rpc.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.primer.android.banks.implementation.rpc.data.datasource.LocalIssuingBankDataSource
import io.primer.android.banks.implementation.rpc.data.datasource.RemoteIssuingBankSuspendDataSource
import io.primer.android.banks.implementation.rpc.data.models.IssuingBankDataResponse
import io.primer.android.banks.implementation.rpc.data.models.IssuingBankResultDataResponse
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBankParams
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class IssuingBankDataRepositoryTest {
    private val mockRemoteDataSource: RemoteIssuingBankSuspendDataSource = mockk()
    private val mockLocalDataSource: LocalIssuingBankDataSource = mockk()
    private val mockConfigurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData> = mockk()

    private val repository =
        IssuingBankDataRepository(
            mockRemoteDataSource,
            mockLocalDataSource,
            mockConfigurationDataSource,
        )

    @Test
    fun `test getIssuingBanks fetches from remote and updates local`() {
        // Arrange
        val params = mockk<IssuingBankParams>(relaxed = true)
        val configurationData =
            mockk<ConfigurationData> {
                every { coreUrl } returns "https://core.url"
            }
        val expectedRemoteResponse =
            listOf(
                IssuingBankDataResponse("bank_id_1", "Bank A", false, ""),
                IssuingBankDataResponse("bank_id_2", "Bank B", true, ""),
            )
        val resultDataResponse = IssuingBankResultDataResponse(expectedRemoteResponse)

        coEvery { mockConfigurationDataSource.get() } returns configurationData
        coEvery { mockRemoteDataSource.execute(any()) } returns resultDataResponse
        coEvery { mockLocalDataSource.update(any()) } returns Unit

        // Act
        runTest {
            val issuingBanks = repository.getIssuingBanks(params)
            val result = issuingBanks.getOrNull()!!

            // Assert
            assertEquals(expectedRemoteResponse.size, result.size)
            assertEquals("bank_id_1", result[0].id)
            assertEquals("Bank A", result[0].name)
            assertEquals(false, result[0].disabled)
            assertEquals("bank_id_2", result[1].id)
            assertEquals("Bank B", result[1].name)
            assertEquals(true, result[1].disabled)
        }
    }

    @Test
    fun `test getCachedIssuingBanks fetches from local`() {
        // Arrange
        val expectedLocalResponse =
            mutableListOf(
                IssuingBankDataResponse("bank_id_1", "Bank A", false, ""),
                IssuingBankDataResponse("bank_id_2", "Bank B", true, ""),
            )

        coEvery { mockLocalDataSource.get() } returns expectedLocalResponse

        // Act
        runTest {
            val issuingBanks = repository.getCachedIssuingBanks()
            assertTrue { issuingBanks.isSuccess }
            val result = issuingBanks.getOrNull()!!
            // Assert
            assertEquals(expectedLocalResponse.size, result.size)
            assertEquals("bank_id_1", result[0].id)
            assertEquals("Bank A", result[0].name)
            assertEquals(false, result[0].disabled)
            assertEquals("bank_id_2", result[1].id)
            assertEquals("Bank B", result[1].name)
            assertEquals(true, result[1].disabled)
        }
    }
}
