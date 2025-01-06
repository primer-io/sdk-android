import io.mockk.coEvery
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.nolpay.implementation.configuration.data.repository.NolPayConfigurationDataRepository
import io.primer.android.nolpay.implementation.configuration.domain.model.NolPayConfig
import io.primer.android.nolpay.implementation.configuration.domain.model.NolPayConfigParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Locale

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class NolPayConfigurationDataRepositoryTest {
    private lateinit var repository: NolPayConfigurationDataRepository
    private val configurationDataSource: CacheConfigurationDataSource = mockk()
    private val primerSettings: PrimerSettings = mockk()

    @BeforeEach
    fun setUp() {
        repository = NolPayConfigurationDataRepository(configurationDataSource, primerSettings)
    }

    @Test
    fun `getPaymentMethodConfiguration should return NolPayConfig`() =
        runTest {
            // Given
            val paymentMethodType = "NOL_PAY"
            val paymentMethodConfigId = "testPaymentMethodConfigId"
            val locale = Locale.US.toLanguageTag()
            val params = NolPayConfigParams(paymentMethodType = paymentMethodType)

            val paymentMethodConfig =
                mockk<PaymentMethodConfigDataResponse> {
                    every { type } returns paymentMethodType
                    every { id } returns paymentMethodConfigId
                }
            val configurationData =
                mockk<ConfigurationData> {
                    every { paymentMethods } returns listOf(paymentMethodConfig)
                }

            coEvery { configurationDataSource.get() } returns configurationData
            every { primerSettings.locale } returns Locale.US

            // When
            val result = repository.getPaymentMethodConfiguration(params).getOrThrow()

            // Then
            assertEquals(NolPayConfig(paymentMethodConfigId, locale), result)
            verify { configurationDataSource.get() }
            verify { primerSettings.locale }
        }
}
