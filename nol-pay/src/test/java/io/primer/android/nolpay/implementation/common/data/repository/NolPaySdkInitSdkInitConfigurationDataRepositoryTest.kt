import io.mockk.coEvery
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.Environment
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.nolpay.implementation.common.data.repository.NolPaySdkInitSdkInitConfigurationDataRepository
import io.primer.android.nolpay.implementation.common.domain.model.NolPayConfiguration
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class NolPaySdkInitSdkInitConfigurationDataRepositoryTest {
    private lateinit var repository: NolPaySdkInitSdkInitConfigurationDataRepository
    private val configurationDataSource: CacheConfigurationDataSource = mockk()

    @BeforeEach
    fun setUp() {
        repository = NolPaySdkInitSdkInitConfigurationDataRepository(configurationDataSource)
    }

    @Test
    fun `getConfiguration should return NolPayConfiguration`() =
        runTest {
            // Given
            val merchantAppId = "testMerchantAppId"
            val envSandbox = Environment.SANDBOX
            val paymentMethodType = PaymentMethodType.NOL_PAY.name
            val paymentMethodConfig =
                mockk<PaymentMethodConfigDataResponse> {
                    every { type } returns paymentMethodType
                    every { options?.merchantAppId } returns merchantAppId
                }
            val configurationData =
                mockk<ConfigurationData> {
                    every { paymentMethods } returns listOf(paymentMethodConfig)
                    every { environment } returns envSandbox
                }

            coEvery { configurationDataSource.get() } returns configurationData

            // When
            val result = repository.getConfiguration().getOrThrow()

            // Then
            assertEquals(NolPayConfiguration(merchantAppId, envSandbox), result)
            verify { configurationDataSource.get() }
        }
}
