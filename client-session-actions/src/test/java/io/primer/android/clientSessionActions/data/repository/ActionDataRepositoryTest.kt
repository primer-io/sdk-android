package io.primer.android.clientSessionActions.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.primer.android.clientSessionActions.data.datasource.RemoteActionDataSource
import io.primer.android.clientSessionActions.data.models.ClientSessionActionsDataRequest
import io.primer.android.clientSessionActions.domain.models.ActionUpdateBillingAddressParams
import io.primer.android.clientSessionActions.domain.models.BaseActionUpdateParams
import io.primer.android.clientSessionActions.domain.repository.ActionRepository
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.datasource.GlobalCacheConfigurationCacheDataSource
import io.primer.android.configuration.data.model.CheckoutModuleDataResponse
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.ConfigurationDataResponse
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerResponse
import io.primer.android.core.utils.BaseDataProvider
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ActionDataRepositoryTest {

    private lateinit var configurationDataSource: CacheConfigurationDataSource
    private lateinit var remoteActionDataSource: RemoteActionDataSource
    private lateinit var globalCacheConfigurationCacheDataSource: GlobalCacheConfigurationCacheDataSource
    private lateinit var clientTokenProvider: BaseDataProvider<String>

    private lateinit var actionRepository: ActionRepository

    @BeforeEach
    fun setUp() {
        configurationDataSource = mockk()
        remoteActionDataSource = mockk()
        globalCacheConfigurationCacheDataSource = mockk()
        clientTokenProvider = mockk()
        actionRepository = ActionDataRepository(
            configurationDataSource = configurationDataSource,
            remoteActionDataSource = remoteActionDataSource,
            globalCacheDataSource = globalCacheConfigurationCacheDataSource,
            clientTokenProvider = clientTokenProvider
        )
    }

    @Test
    fun `updateClientActions should update client actions successfully`() = runTest {
        val configurationData = mockk<ConfigurationData> {
            every { pciUrl } returns "https://example.com"
            every { copy(clientSession = any()) } returns this
            every { copy(checkoutModules = any()) } returns this
        }

        val clientSessionResponse = mockk<ClientSessionDataResponse> {
            every { toClientSessionData() } returns mockk()
        }
        val checkoutModulesResponse = mockk<List<CheckoutModuleDataResponse>>()

        val dataResponse = mockk<ConfigurationDataResponse> {
            every { clientSession } returns clientSessionResponse
            every { checkoutModules } returns checkoutModulesResponse
        }

        val params: BaseActionUpdateParams = mockk<ActionUpdateBillingAddressParams>(relaxed = true)

        coEvery { configurationDataSource.get() } returns configurationData
        coEvery { remoteActionDataSource.execute(any()) } returns PrimerResponse(dataResponse, emptyMap())
        coEvery { configurationDataSource.update(any()) } returns Unit
        every { clientTokenProvider.provide() } returns "clientToken"
        every { globalCacheConfigurationCacheDataSource.update(any()) } returns Unit

        val result = actionRepository.updateClientActions(listOf(params))

        coVerify { configurationDataSource.get() }
        coVerify { remoteActionDataSource.execute(any<BaseRemoteHostRequest<ClientSessionActionsDataRequest>>()) }
        coVerify { configurationDataSource.update(any()) }

        assertNotNull(result.getOrNull())
    }
}
