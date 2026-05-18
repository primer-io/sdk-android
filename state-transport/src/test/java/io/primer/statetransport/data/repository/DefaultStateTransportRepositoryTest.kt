@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.statetransport.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.configuration.data.model.PaymentMethodRemoteConfigOptions
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.network.PrimerResponse
import io.primer.android.core.utils.BaseDataProvider
import io.primer.statetransport.data.datasource.RemoteInstructionsDataSource
import io.primer.statetransport.data.datasource.RemotePayDataSource
import io.primer.statetransport.data.model.ClientInstructionDataResponse
import io.primer.statetransport.data.model.ClientInstructionType
import io.primer.statetransport.data.model.ClientSessionInstructionResponse
import io.primer.statetransport.domain.model.ClientInstructions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class DefaultStateTransportRepositoryTest {

    @MockK
    lateinit var configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>

    @MockK
    lateinit var remoteStartDataSource: RemotePayDataSource

    @MockK
    lateinit var remoteInstructionsDataSource: RemoteInstructionsDataSource

    @MockK
    lateinit var applicationIdProvider: BaseDataProvider<String>

    private lateinit var repository: DefaultStateTransportRepository

    private val paymentMethodConfig = mockk<PaymentMethodConfigDataResponse>(relaxed = true) {
        every { type } returns "PAYMENT_CARD"
        every { id } returns "config_123"
        every { options } returns mockk<PaymentMethodRemoteConfigOptions> {
            every { merchantAccountId } returns "merchant_456"
        }
    }

    private val configurationData = mockk<ConfigurationData>(relaxed = true) {
        every { pciUrl } returns "https://pci.example.com"
        every { paymentMethods } returns listOf(paymentMethodConfig)
    }

    @BeforeEach
    fun setUp() {
        every { configurationDataSource.get() } returns configurationData
        every { applicationIdProvider.provide() } returns "app_123"
        repository = DefaultStateTransportRepository(
            configurationDataSource = configurationDataSource,
            remoteStartDataSource = remoteStartDataSource,
            remoteInstructionsDataSource = remoteInstructionsDataSource,
            applicationIdProvider = applicationIdProvider,
        )
    }

    @Test
    fun `start should return Wait instruction on WAIT response`() = runTest {
        val instruction = ClientInstructionDataResponse(
            type = ClientInstructionType.WAIT,
            pollDelayMilliseconds = 3000L,
            payload = null,
        )
        coEvery { remoteStartDataSource.execute(any()) } returns PrimerResponse(
            statusCode = 200,
            body = ClientSessionInstructionResponse(clientInstruction = instruction),
            headers = emptyMap(),
        )

        val result = repository.start("session_1", "PAYMENT_CARD", "https://return.com")

        assertTrue(result.isSuccess)
        val wait = result.getOrThrow() as ClientInstructions.Wait
        assertEquals(3000L, wait.pollDelayMilliseconds)
    }

    @Test
    fun `start should return failure when payment method config not found`() = runTest {
        val result = repository.start("session_1", "UNKNOWN_TYPE", "https://return.com")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not found") == true)
    }

    @Test
    fun `start should return failure when remote data source throws`() = runTest {
        coEvery { remoteStartDataSource.execute(any()) } throws RuntimeException("network error")

        val result = repository.start("session_1", "PAYMENT_CARD", "https://return.com")

        assertTrue(result.isFailure)
    }

    @Test
    fun `fetchInstructions should return instruction from remote`() = runTest {
        val instruction = ClientInstructionDataResponse(
            type = ClientInstructionType.EXECUTE,
            pollDelayMilliseconds = 1000L,
            payload = """{"action":"tokenize"}""",
        )
        coEvery { remoteInstructionsDataSource.execute(any()) } returns PrimerResponse(
            statusCode = 200,
            body = ClientSessionInstructionResponse(clientInstruction = instruction),
            headers = emptyMap(),
        )

        val result = repository.fetchInstructions("session_1")

        assertTrue(result.isSuccess)
        val execute = result.getOrThrow() as ClientInstructions.Execute
        assertEquals(1000L, execute.pollDelayMilliseconds)
        assertEquals("""{"action":"tokenize"}""", execute.payload)
    }

    @Test
    fun `fetchInstructions should return failure when remote throws`() = runTest {
        coEvery { remoteInstructionsDataSource.execute(any()) } throws RuntimeException("timeout")

        val result = repository.fetchInstructions("session_1")

        assertTrue(result.isFailure)
    }
}
