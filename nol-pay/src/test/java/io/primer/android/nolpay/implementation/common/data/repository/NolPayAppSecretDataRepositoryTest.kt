import android.os.Build
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.nolpay.implementation.common.data.datasource.RemoteNolPaySecretDataSource
import io.primer.android.nolpay.implementation.common.data.model.NolPaySecretDataRequest
import io.primer.android.nolpay.implementation.common.data.model.NolPaySecretDataResponse
import io.primer.android.nolpay.implementation.common.data.repository.NolPayAppSecretDataRepository
import io.primer.android.nolpay.modifyClassProperty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class NolPayAppSecretDataRepositoryTest {
    private lateinit var repository: NolPayAppSecretDataRepository
    private val configurationDataSource: CacheConfigurationDataSource = mockk()
    private val nolPaySecretDataSource: RemoteNolPaySecretDataSource = mockk()

    @BeforeEach
    fun setUp() {
        repository = NolPayAppSecretDataRepository(configurationDataSource, nolPaySecretDataSource)
    }

    @Test
    fun `getAppSecret should return sdkSecret from NolPaySecretDataSource`() =
        runTest {
            // Given
            val sdkId = "testSdkId"
            val appId = "testAppId"
            val url = "https://example.com"
            val sdkSecret = "testSdkSecret"
            val manufacturer = "Samsung"
            val model = "S20"

            modifyClassProperty<Build>("MANUFACTURER", manufacturer)
            modifyClassProperty<Build>("MODEL", model)

            val configuration =
                mockk<ConfigurationData>(relaxed = true) {
                    every { coreUrl } returns url
                }
            val requestData =
                NolPaySecretDataRequest(
                    sdkId = sdkId,
                    appId = appId,
                    deviceVendor = manufacturer,
                    deviceModel = model,
                )
            val request = BaseRemoteHostRequest(url, requestData)
            val expectedResponse = NolPaySecretDataResponse(sdkSecret)

            coEvery { configurationDataSource.get() } returns configuration
            coEvery { nolPaySecretDataSource.execute(request) } returns expectedResponse

            // When
            val result = repository.getAppSecret(sdkId, appId).getOrThrow()

            // Then
            assertEquals(sdkSecret, result)
            coVerify(exactly = 1) { configurationDataSource.get() }
            coVerify(exactly = 1) { nolPaySecretDataSource.execute(request) }
        }
}
