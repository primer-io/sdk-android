package io.primer.android.data.configuration.repository

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.checkUnnecessaryStub
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.analytics.data.helper.TimerEventProvider
import io.primer.android.data.configuration.datasource.ConfigurationCache
import io.primer.android.data.configuration.datasource.GlobalConfigurationCacheDataSource
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.datasource.RemoteConfigurationDataSource
import io.primer.android.data.configuration.datasource.RemoteConfigurationResourcesDataSource
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.data.configuration.models.ConfigurationDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.data.utils.PrimerSessionConstants
import io.primer.android.domain.session.CachePolicy
import io.primer.android.domain.session.models.Configuration
import io.primer.android.http.PrimerResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ConfigurationDataRepositoryTest {

    private lateinit var repository: ConfigurationDataRepository

    @RelaxedMockK
    internal lateinit var remoteConfigurationDataSource: RemoteConfigurationDataSource

    @RelaxedMockK
    internal lateinit var remoteConfigurationResourcesDataSource: RemoteConfigurationResourcesDataSource

    @RelaxedMockK
    internal lateinit var localConfigurationDataSource: LocalConfigurationDataSource

    @RelaxedMockK
    internal lateinit var localClientTokenDataSource: LocalClientTokenDataSource

    @RelaxedMockK
    internal lateinit var globalConfigurationCache: GlobalConfigurationCacheDataSource

    @RelaxedMockK
    internal lateinit var primerConfig: PrimerConfig

    @RelaxedMockK
    internal lateinit var timerEventProvider: TimerEventProvider

    @BeforeEach
    fun setUp() {
        mockkStatic(Uri::class)
        val uriMock = mockk<Uri>()
        val uriBuilder = mockk<Uri.Builder>()
        mockkConstructor(Uri.Builder::class).also {
            every { anyConstructed<Uri.Builder>().scheme(any()) } returns uriBuilder
            every { uriMock.buildUpon() } returns uriBuilder
            every { uriBuilder.appendQueryParameter(any(), any()) } returns uriBuilder
            every { uriBuilder.build() } returns uriMock
        }
        every { Uri.parse(any()) } returns uriMock

        MockKAnnotations.init(this, relaxed = true)
        repository = ConfigurationDataRepository(
            remoteConfigurationDataSource = remoteConfigurationDataSource,
            remoteConfigurationResourcesDataSource = remoteConfigurationResourcesDataSource,
            localConfigurationDataSource = localConfigurationDataSource,
            localClientTokenDataSource = localClientTokenDataSource,
            globalConfigurationCache = globalConfigurationCache,
            primerConfig = primerConfig,
            timerEventProvider = timerEventProvider,
            getCurrentTimeMillis = { CURRENT_TIME_IN_MILLIS }
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Uri::class)
        val mocks = arrayOf(
            remoteConfigurationDataSource,
            remoteConfigurationResourcesDataSource,
            localConfigurationDataSource,
            localClientTokenDataSource,
            globalConfigurationCache,
            primerConfig,
            timerEventProvider
        )
        confirmVerified(*mocks)
        checkUnnecessaryStub(*mocks)
    }

    @Test
    fun `fetchConfiguration with ForceCache should serve configuration from local cache`() = runTest {
        val configurationData = mockk<ConfigurationData>()
        val configuration = mockk<Configuration>()
        every { localConfigurationDataSource.get() } returns flowOf(configurationData)
        every { configurationData.toConfiguration() } returns configuration

        val result = repository.fetchConfiguration(CachePolicy.ForceCache).toList()

        assertEquals(listOf(configuration), result)
        verify { localConfigurationDataSource.get() }
        verify { configurationData.toConfiguration() }
    }

    @Test
    fun `fetchConfiguration with ForceNetwork should call remote API and should not get configuration from cache`() = runTest {
        val configurationData = mockk<ConfigurationData>(relaxed = true)
        val configurationDataResponse = mockk<ConfigurationDataResponse>(relaxed = true)
        val configurationResponse = mockk<PrimerResponse<ConfigurationDataResponse>>(relaxed = true)
        every { configurationResponse.body } returns configurationDataResponse
        every { configurationResponse.headers } returns mapOf(
            PrimerSessionConstants.PRIMER_SESSION_CACHE_TTL_HEADER to listOf(
                "$DEFAULT_TTL_CACHE_HEADER_VALUE_IN_SECONDS"
            )
        )
        every { primerConfig.clientTokenBase64 } returns VALID_TOKEN
        val configuration = mockk<Configuration>(relaxed = true)
        every { configurationDataResponse.toConfigurationData(any()) } returns configurationData
        every { configurationData.toConfiguration() } returns configuration
        every { remoteConfigurationDataSource.execute(any()) } returns flowOf(
            configurationResponse
        )
        every { remoteConfigurationResourcesDataSource.execute(any()) } returns flowOf(
            emptyList()
        )
        every { globalConfigurationCache.update(any()) } just Runs
        every { localConfigurationDataSource.update(any()) } just Runs
        every { timerEventProvider.getTimerEventProvider() } returns MutableStateFlow(null)

        val result = repository.fetchConfiguration(CachePolicy.ForceNetwork).toList()
        assertEquals(listOf(configuration), result)

        coVerify { remoteConfigurationDataSource.execute(any()) }
        coVerify { localConfigurationDataSource.update(any()) }
        coVerify { configurationData.toConfiguration() }
        verify(exactly = 0) { globalConfigurationCache.get() }
        verify { globalConfigurationCache.clear() }
        verify {
            globalConfigurationCache.update(
                ConfigurationCache(
                    validUntil = CURRENT_TIME_IN_MILLIS + DEFAULT_TTL_CACHE_HEADER_VALUE_IN_MILLISECONDS,
                    clientToken = VALID_TOKEN
                ) to configurationDataResponse
            )
        }
        verify { configurationDataResponse.toConfigurationData(any()) }
        verify { configurationData.toConfiguration() }
        verify { remoteConfigurationResourcesDataSource.execute(any()) }
        verify { localConfigurationDataSource.update(configurationData) }
        verify { primerConfig.clientTokenBase64 }
        verify { localClientTokenDataSource.get() }
        verify(exactly = 2) { timerEventProvider.getTimerEventProvider() }
    }

    @Test
    fun `fetchConfiguration with CacheFirst and valid cache should serve configuration from global cache`() = runTest {
        val configurationData = mockk<ConfigurationData>(relaxed = true)
        val configurationDataResponse = mockk<ConfigurationDataResponse>(relaxed = true)
        val configurationResponse = mockk<PrimerResponse<ConfigurationDataResponse>>(relaxed = true)
        every { configurationResponse.body } returns configurationDataResponse
        val configuration = mockk<Configuration>(relaxed = true)
        every { globalConfigurationCache.get() } returns mockk {
            every { first } returns mockk {
                every { clientToken } returns VALID_TOKEN
                every { validUntil } returns CURRENT_TIME_IN_MILLIS + 1000
            }
            every { second } returns configurationResponse.body
        }
        every { primerConfig.clientTokenBase64 } returns VALID_TOKEN
        every { configurationDataResponse.toConfigurationData(any()) } returns configurationData
        every { configurationData.toConfiguration() } returns configuration
        every { remoteConfigurationResourcesDataSource.execute(any()) } returns flowOf(mockk())
        every { localConfigurationDataSource.update(any()) } just Runs
        every { timerEventProvider.getTimerEventProvider() } returns MutableStateFlow(null)

        val result = repository.fetchConfiguration(CachePolicy.CacheFirst).toList()
        assertEquals(configuration, result.first())

        verify { globalConfigurationCache.get() }
        verify { configurationDataResponse.toConfigurationData(any()) }
        verify { configurationData.toConfiguration() }
        verify { remoteConfigurationResourcesDataSource.execute(any()) }
        verify { localConfigurationDataSource.update(configurationData) }
        verify { primerConfig.clientTokenBase64 }
        verify(exactly = 2) { timerEventProvider.getTimerEventProvider() }
    }

    @Test
    fun `fetchConfiguration with CacheFirst and invalid cache should call remote API and should not get configuration from cache`() = runTest {
        val configurationData = mockk<ConfigurationData>(relaxed = true)
        val configurationDataResponse = mockk<ConfigurationDataResponse>(relaxed = true)
        val configurationResponse = mockk<PrimerResponse<ConfigurationDataResponse>>(relaxed = true)
        every { configurationResponse.body } returns configurationDataResponse
        every { configurationResponse.headers } returns mapOf(
            PrimerSessionConstants.PRIMER_SESSION_CACHE_TTL_HEADER to listOf(
                "$DEFAULT_TTL_CACHE_HEADER_VALUE_IN_SECONDS"
            )
        )
        val configuration = mockk<Configuration>(relaxed = true)
        every { globalConfigurationCache.get() } returns mockk {
            every { first } returns mockk {
                every { clientToken } returns INVALID_TOKEN
                every { validUntil } returns CURRENT_TIME_IN_MILLIS - 1000
            }
            every { second } returns configurationResponse.body
        }
        every { primerConfig.clientTokenBase64 } returns VALID_TOKEN
        every { remoteConfigurationDataSource.execute(any()) } returns flowOf(
            configurationResponse
        )
        every { remoteConfigurationResourcesDataSource.execute(any()) } returns flowOf(emptyList())

        every { configurationDataResponse.toConfigurationData(any()) } returns configurationData
        every { configurationData.toConfiguration() } returns configuration
        every { globalConfigurationCache.update(any()) } just Runs
        every { localConfigurationDataSource.update(any()) } just Runs

        val result = repository.fetchConfiguration(CachePolicy.CacheFirst).toList()

        assertEquals(listOf(configuration), result)

        coVerify { remoteConfigurationDataSource.execute(any()) }
        coVerify { localConfigurationDataSource.update(configurationData) }
        coVerify { configurationData.toConfiguration() }
        verify { globalConfigurationCache.get() }
        verify {
            globalConfigurationCache.update(
                ConfigurationCache(
                    validUntil = DEFAULT_TTL_CACHE_HEADER_VALUE_IN_MILLISECONDS + CURRENT_TIME_IN_MILLIS,
                    clientToken = VALID_TOKEN
                ) to configurationDataResponse
            )
        }
        verify { globalConfigurationCache.clear() }
        verify { configurationData.toConfiguration() }
        verify { remoteConfigurationResourcesDataSource.execute(any()) }
        verify { localConfigurationDataSource.update(configurationData) }
        verify { primerConfig.clientTokenBase64 }
        verify { localClientTokenDataSource.get() }
        verify(exactly = 2) {
            timerEventProvider.getTimerEventProvider()
        }
    }

    private companion object {

        const val VALID_TOKEN = "validToken"
        const val INVALID_TOKEN = "invalidToken"
        const val DEFAULT_TTL_CACHE_HEADER_VALUE_IN_SECONDS = 300L
        const val CURRENT_TIME_IN_MILLIS = 10000L
        const val DEFAULT_TTL_CACHE_HEADER_VALUE_IN_MILLISECONDS = DEFAULT_TTL_CACHE_HEADER_VALUE_IN_SECONDS * 1000
    }
}
