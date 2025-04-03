package io.primer.android.surcharge.domain

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.configuration.data.extensions.surcharges
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.domain.model.ClientSession
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.model.Surcharge
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.None
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SurchargeInteractorTest {

    private lateinit var configurationRepository: ConfigurationRepository
    private lateinit var surchargeInteractor: SurchargeInteractor

    @BeforeEach
    fun setup() {
        mockkStatic(EXT_FILE_NAME)
        configurationRepository = mockk()
        surchargeInteractor = SurchargeInteractor(configurationRepository = configurationRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(EXT_FILE_NAME)
        confirmVerified(configurationRepository)
    }

    @Test
    fun `execute should return surcharges from configuration repository`() {
        val expectedSurcharges = mapOf(
            "PAYMENT_CARD" to Surcharge.CardNetworksSurcharge(
                mapOf(
                    CardNetwork.Type.VISA.name to 5,
                    CardNetwork.Type.MASTERCARD.name to 10,
                ),
            ),
            "PAYPAL" to Surcharge.PaymentMethodSurcharge(amount = 10),
        )

        val mockResponse = mockk<ClientSessionDataResponse> {
            every { paymentMethod?.surcharges() } returns expectedSurcharges
        }
        val mockClientSession = mockk<ClientSession> {
            every { clientSessionDataResponse } returns mockResponse
        }
        val mockConfiguration = mockk<Configuration> {
            every { clientSession } returns mockClientSession
        }
        every { configurationRepository.getConfiguration() } returns mockConfiguration

        val result = surchargeInteractor.execute(None)

        assertEquals(expectedSurcharges, result)

        verify { configurationRepository.getConfiguration() }
    }

    @Test
    fun `execute should return empty map when no surcharges exist`() {
        val mockResponse = mockk<ClientSessionDataResponse> {
            every { paymentMethod?.surcharges() } returns emptyMap()
        }
        val mockClientSession = mockk<ClientSession> {
            every { clientSessionDataResponse } returns mockResponse
        }
        val mockConfiguration = mockk<Configuration> {
            every { clientSession } returns mockClientSession
        }
        every { configurationRepository.getConfiguration() } returns mockConfiguration

        val result = surchargeInteractor.execute(None)

        assertEquals(emptyMap<String, Surcharge>(), result)

        verify { configurationRepository.getConfiguration() }
    }

    @Test
    fun `execute should return empty map when surcharges is null`() {
        val mockResponse = mockk<ClientSessionDataResponse> {
            every { paymentMethod?.surcharges() } returns null
        }
        val mockClientSession = mockk<ClientSession> {
            every { clientSessionDataResponse } returns mockResponse
        }
        val mockConfiguration = mockk<Configuration> {
            every { clientSession } returns mockClientSession
        }
        every { configurationRepository.getConfiguration() } returns mockConfiguration

        val result = surchargeInteractor.execute(None)

        assertEquals(emptyMap<String, Surcharge>(), result)

        verify { configurationRepository.getConfiguration() }
    }

    private companion object {

        private const val EXT_FILE_NAME =
            "io.primer.android.configuration.data.extensions.PaymentMethodDataResponseKt"
    }
}
