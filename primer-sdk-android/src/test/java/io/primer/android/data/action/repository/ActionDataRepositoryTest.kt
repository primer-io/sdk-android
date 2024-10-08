package io.primer.android.data.action.repository

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.primer.android.data.action.datasource.RemoteActionDataSource
import io.primer.android.data.action.models.ClientSessionActionsDataRequest
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.ConfigurationCache
import io.primer.android.data.configuration.datasource.GlobalConfigurationCacheDataSource
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.CheckoutModuleDataResponse
import io.primer.android.data.configuration.models.ClientSessionDataResponse
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.data.configuration.models.ConfigurationDataResponse
import io.primer.android.data.configuration.models.Environment
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.utils.PrimerSessionConstants
import io.primer.android.domain.ClientSessionData
import io.primer.android.domain.action.models.ActionUpdateCustomerDetailsParams
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.action.repository.ActionRepository
import io.primer.android.http.PrimerResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ActionDataRepositoryTest {

    private val initialClientSession = ClientSessionDataResponse(
        clientSessionId = "fakeSessionId",
        customerId = "fakeCustomerId",
        orderId = "fakeOrderId",
        testId = "fakeTestId",
        amount = 1000,
        currencyCode = "USD",
        customer = null,
        order = null,
        paymentMethod = null
    )

    private val remoteDataResponse = ClientSessionDataResponse(
        clientSessionId = "fakeSessionId2",
        customerId = "fakeCustomerId",
        orderId = "fakeOrderId",
        testId = "fakeTestId",
        amount = 1000,
        currencyCode = "USD",
        customer = null,
        order = null,
        paymentMethod = null
    )

    private val mockCheckoutModules = listOf(mockk<CheckoutModuleDataResponse>())
    private val configurationDataResponse = ConfigurationDataResponse(
        pciUrl = "https://example.com/pci",
        coreUrl = "https://example.com/core",
        binDataUrl = "https://example.com/bin",
        assetsUrl = "https://example.com/assets",
        paymentMethods = emptyList(),
        checkoutModules = mockCheckoutModules,
        keys = null,
        clientSession = remoteDataResponse,
        environment = Environment.DEV,
        primerAccountId = "account123"
    )

    private val remoteConfigurationData = ConfigurationData(
        pciUrl = "https://example.com/pci",
        coreUrl = "https://example.com/core",
        binDataUrl = "https://example.com/bin",
        assetsUrl = "https://example.com/assets",
        paymentMethods = emptyList(),
        checkoutModules = mockCheckoutModules,
        keys = null,
        clientSession = remoteDataResponse,
        environment = Environment.DEV,
        primerAccountId = "account123",
        iconsDisplayMetadata = emptyList()
    )

    private val localConfigurationData = ConfigurationData(
        pciUrl = "https://example.com/pci",
        coreUrl = "https://example.com/core",
        binDataUrl = "https://example.com/bin",
        assetsUrl = "https://example.com/assets",
        paymentMethods = emptyList(),
        checkoutModules = emptyList(),
        keys = null,
        clientSession = initialClientSession,
        environment = Environment.DEV,
        primerAccountId = "account123",
        iconsDisplayMetadata = emptyList()
    )

    private val localConfigurationDataSource: LocalConfigurationDataSource = mockk()
    private val remoteActionDataSource: RemoteActionDataSource = mockk()
    private val globalConfigurationCache: GlobalConfigurationCacheDataSource = mockk()
    private val primerConfig: PrimerConfig = mockk {
        every { clientTokenBase64 } returns "fakeClientTokenBase64"
    }

    private val currentTime = 10000L
    private lateinit var actionRepository: ActionRepository

    @BeforeEach
    fun setUp() {
        actionRepository = ActionDataRepository(
            localConfigurationDataSource,
            remoteActionDataSource,
            globalConfigurationCache,
            primerConfig
        ) { currentTime }
    }

    @Test
    fun `updateClientActions should update client actions and cache configuration changes`() = runTest {
        // Arrange
        val params = ActionUpdateCustomerDetailsParams(
            firstName = "John",
            lastName = "Doe",
            emailAddress = null
        )

        val expectedClientSessionData = ClientSessionData(
            PrimerClientSession(
                customerId = "fakeCustomerId",
                orderId = "fakeOrderId",
                currencyCode = "USD",
                totalAmount = 1000,
                lineItems = null,
                orderDetails = null,
                customer = null,
                paymentMethod = null,
                fees = null
            )
        )

        val expectedConfigurationCache = ConfigurationCache(
            validUntil = 3610000,
            clientToken = "fakeClientTokenBase64"
        )
        val expectedBaseRemoteRequest = BaseRemoteRequest(
            configuration = localConfigurationData,
            data = ClientSessionActionsDataRequest(
                listOf(
                    ClientSessionActionsDataRequest.SetCustomerFirstName("John"),
                    ClientSessionActionsDataRequest.SetCustomerLastName("Doe")
                )
            )
        )
        val expectedPrimerResponse = PrimerResponse(
            body = configurationDataResponse,
            headers = mapOf(PrimerSessionConstants.PRIMER_SESSION_CACHE_TTL_HEADER to listOf("3600"))
        )

        coEvery { localConfigurationDataSource.get() } returns flowOf(localConfigurationData)
        coEvery { remoteActionDataSource.execute(any()) } returns flowOf(expectedPrimerResponse)
        coEvery { globalConfigurationCache.update(any()) } just Runs
        coEvery { localConfigurationDataSource.update(any()) } just Runs

        // Act
        val result = actionRepository.updateClientActions(listOf(params)).first()

        // Assert
        assertEquals(expectedClientSessionData, result)
        coVerify { localConfigurationDataSource.get() }
        coVerify { remoteActionDataSource.execute(expectedBaseRemoteRequest) }
        coVerify { globalConfigurationCache.update(expectedConfigurationCache to expectedPrimerResponse.body) }
        coVerify { localConfigurationDataSource.update(remoteConfigurationData) }
    }
}
