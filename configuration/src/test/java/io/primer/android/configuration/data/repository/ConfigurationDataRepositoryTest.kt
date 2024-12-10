package io.primer.android.configuration.data.repository

import android.content.SharedPreferences
import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.checkUnnecessaryStub
import io.mockk.coEvery
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
import io.primer.android.analytics.data.models.TimerProperties
import io.primer.android.configuration.PrimerSessionConstants
import io.primer.android.configuration.data.datasource.ConfigurationCache
import io.primer.android.configuration.data.datasource.GlobalConfigurationCacheDataSource
import io.primer.android.configuration.data.datasource.LocalConfigurationDataSource
import io.primer.android.configuration.data.datasource.RemoteConfigurationDataSource
import io.primer.android.configuration.data.datasource.RemoteConfigurationResourcesDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.ConfigurationDataResponse
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.core.data.network.PrimerResponse
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.core.utils.EventFlowProvider
import kotlinx.coroutines.flow.MutableStateFlow
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
    internal lateinit var sharedPreferences: SharedPreferences

    @RelaxedMockK
    internal lateinit var remoteConfigurationDataSource: RemoteConfigurationDataSource

    @RelaxedMockK
    internal lateinit var remoteConfigurationResourcesDataSource: RemoteConfigurationResourcesDataSource

    @RelaxedMockK
    internal lateinit var localConfigurationDataSource: LocalConfigurationDataSource

    @RelaxedMockK
    internal lateinit var configurationUrlProvider: BaseDataProvider<String>

    @RelaxedMockK
    internal lateinit var clientTokenProvider: BaseDataProvider<String>

    @RelaxedMockK
    internal lateinit var globalConfigurationCache: GlobalConfigurationCacheDataSource

    @RelaxedMockK
    internal lateinit var logReporter: LogReporter

    @RelaxedMockK
    internal lateinit var timerEventProvider: EventFlowProvider<TimerProperties>

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
            configurationUrlProvider = configurationUrlProvider,
            clientTokenProvider = clientTokenProvider,
            globalConfigurationCache = globalConfigurationCache,
            timerEventProvider = timerEventProvider,
            logReporter = logReporter,
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
            configurationUrlProvider,
            clientTokenProvider,
            globalConfigurationCache,
            timerEventProvider
        )
        confirmVerified(*mocks)
        checkUnnecessaryStub(*mocks)
    }

    @Test
    fun `fetchConfiguration with ForceCache should serve configuration from local cache`() = runTest {
        val configurationData = mockk<ConfigurationData>()
        val configuration = mockk<Configuration>()
        coEvery { localConfigurationDataSource.get() } returns configurationData
        every { configurationData.toConfiguration() } returns configuration

        val result = repository.fetchConfiguration(CachePolicy.ForceCache).getOrThrow()
        assertEquals(configuration, result)

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
        every { clientTokenProvider.provide() } returns VALID_TOKEN
        every { configurationUrlProvider.provide() } returns CONFIGURATION_URL
        val configuration = mockk<Configuration>(relaxed = true)
        every { configurationDataResponse.toConfigurationData(any()) } returns configurationData
        every { configurationData.toConfiguration() } returns configuration
        coEvery { remoteConfigurationDataSource.execute(any()) } returns configurationResponse
        coEvery { remoteConfigurationResourcesDataSource.execute(any()) } returns emptyList()

        every { globalConfigurationCache.update(any()) } just Runs
        every { localConfigurationDataSource.update(any()) } just Runs
        every { timerEventProvider.getEventProvider() } returns MutableStateFlow(null)

        val result = repository.fetchConfiguration(CachePolicy.ForceNetwork).getOrThrow()
        assertEquals(configuration, result)

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
        coVerify { remoteConfigurationResourcesDataSource.execute(any()) }
        verify { localConfigurationDataSource.update(configurationData) }
        coVerify { clientTokenProvider.provide() }
        verify { configurationUrlProvider.provide() }
        verify(exactly = 2) { timerEventProvider.getEventProvider() }
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
        every { clientTokenProvider.provide() } returns VALID_TOKEN
        every { configurationDataResponse.toConfigurationData(any()) } returns configurationData
        every { configurationData.toConfiguration() } returns configuration
        coEvery { remoteConfigurationResourcesDataSource.execute(any()) } returns mockk()
        every { localConfigurationDataSource.update(any()) } just Runs
        every { timerEventProvider.getEventProvider() } returns MutableStateFlow(null)

        val result = repository.fetchConfiguration(CachePolicy.CacheFirst).getOrThrow()
        assertEquals(configuration, result)

        verify { globalConfigurationCache.get() }
        verify { configurationDataResponse.toConfigurationData(any()) }
        verify { configurationData.toConfiguration() }
        coVerify { remoteConfigurationResourcesDataSource.execute(any()) }
        verify { localConfigurationDataSource.update(configurationData) }
        verify { clientTokenProvider.provide() }
        verify(exactly = 2) { timerEventProvider.getEventProvider() }
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
        every { clientTokenProvider.provide() } returns VALID_TOKEN
        every { configurationUrlProvider.provide() } returns CONFIGURATION_URL
        coEvery { remoteConfigurationDataSource.execute(any()) } returns configurationResponse
        coEvery { remoteConfigurationResourcesDataSource.execute(any()) } returns emptyList()

        every { configurationDataResponse.toConfigurationData(any()) } returns configurationData
        every { configurationData.toConfiguration() } returns configuration
        every { globalConfigurationCache.update(any()) } just Runs
        every { localConfigurationDataSource.update(any()) } just Runs

        val result = repository.fetchConfiguration(CachePolicy.CacheFirst).getOrThrow()
        assertEquals(configuration, result)

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
        coVerify { remoteConfigurationResourcesDataSource.execute(any()) }
        verify { localConfigurationDataSource.update(configurationData) }
        verify { clientTokenProvider.provide() }
        verify { configurationUrlProvider.provide() }
        verify(exactly = 2) {
            timerEventProvider.getEventProvider()
        }
    }

    private companion object {

        const val VALID_TOKEN = "validToken"
        const val INVALID_TOKEN = "invalidToken"
        const val CONFIGURATION_URL = "https://primer.io/configuration"
        const val DEFAULT_TTL_CACHE_HEADER_VALUE_IN_SECONDS = 300L
        const val CURRENT_TIME_IN_MILLIS = 10000L
        const val DEFAULT_TTL_CACHE_HEADER_VALUE_IN_MILLISECONDS = DEFAULT_TTL_CACHE_HEADER_VALUE_IN_SECONDS * 1000
    }
}
