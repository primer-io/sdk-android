package io.primer.android.threeds.data.repository

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.exception.MissingConfigurationException
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.Environment
import io.primer.android.threeds.InstantExecutorExtension
import io.primer.android.threeds.data.exception.ThreeDsUnknownProtocolException
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ThreeDsConfigurationDataRepositoryTest {
    @RelaxedMockK
    internal lateinit var configurationDataSource: CacheConfigurationDataSource

    private lateinit var repository: ThreeDsConfigurationDataRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        repository = ThreeDsConfigurationDataRepository(configurationDataSource = configurationDataSource)
    }

    @Test
    fun `getConfiguration should return correct ThreeDsKeysParams when there is local cached configuration`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)
        coEvery { configurationDataSource.get() }.returns(configurationData)

        runTest {
            val threeDsParams = repository.getConfiguration().getOrThrow()
            assertEquals(configurationData.environment, threeDsParams.environment)
            assertEquals(configurationData.keys?.netceteraApiKey, threeDsParams.apiKey)
            assertEquals(
                configurationData.keys?.threeDSecureIoCertificates,
                threeDsParams.threeDsCertificates,
            )
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getConfiguration() }
    }

    @Test
    fun `getConfiguration should return MissingConfigurationException when there is no local cached configuration`() {
        every { configurationDataSource.get() }.throws(
            MissingConfigurationException(null),
        )
        assertThrows<MissingConfigurationException> {
            runTest {
                repository.getConfiguration().getOrThrow()
            }
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getConfiguration() }
    }

    @Test
    fun `getPreAuthConfiguration should return V_210 when supportedThreeDsProtocolVersions contains only 2_1_0`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)

        val supportedProtocolVersions = listOf(ProtocolVersion.V_210.versionNumber)
        every { configurationData.environment }.returns(Environment.PRODUCTION)
        coEvery { configurationDataSource.get() }.returns(configurationData)

        runTest {
            val preAuthConfiguration = repository.getPreAuthConfiguration(supportedProtocolVersions).getOrThrow()
            assertEquals(listOf(ProtocolVersion.V_210), preAuthConfiguration.protocolVersions)
            assertEquals(Environment.PRODUCTION, preAuthConfiguration.environment)
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getPreAuthConfiguration(supportedProtocolVersions) }
    }

    @Test
    fun `getPreAuthConfiguration should return V_220 when supportedThreeDsProtocolVersions contains only 2_2_0`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)

        val supportedProtocolVersions = listOf(ProtocolVersion.V_220.versionNumber)

        every { configurationData.environment }.returns(Environment.PRODUCTION)
        coEvery { configurationDataSource.get() }.returns(configurationData)

        runTest {
            val preAuthConfiguration = repository.getPreAuthConfiguration(supportedProtocolVersions).getOrThrow()
            assertEquals(listOf(ProtocolVersion.V_220), preAuthConfiguration.protocolVersions)
            assertEquals(Environment.PRODUCTION, preAuthConfiguration.environment)
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getPreAuthConfiguration(supportedProtocolVersions) }
    }

    @Test
    fun `getPreAuthConfiguration should return V_220 when supportedThreeDsProtocolVersions contains 2_1_0, 2_2_0`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)

        val supportedProtocolVersions = listOf(ProtocolVersion.V_210.versionNumber, ProtocolVersion.V_220.versionNumber)

        every { configurationData.environment }.returns(Environment.PRODUCTION)
        coEvery { configurationDataSource.get() }.returns(configurationData)

        runTest {
            val preAuthConfiguration = repository.getPreAuthConfiguration(supportedProtocolVersions).getOrThrow()
            assertEquals(
                listOf(ProtocolVersion.V_210, ProtocolVersion.V_220),
                preAuthConfiguration.protocolVersions,
            )
            assertEquals(Environment.PRODUCTION, preAuthConfiguration.environment)
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getPreAuthConfiguration(supportedProtocolVersions) }
    }

    @Test
    fun `getPreAuthConfiguration should filter invalid threeds protocol versions`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)

        val supportedProtocolVersions =
            listOf(
                ProtocolVersion.V_210.versionNumber,
                ProtocolVersion.V_220.versionNumber,
                ProtocolVersion.V_231.versionNumber,
                UNSUPPORTED_PROTOCOL_VERSION,
            )

        every { configurationData.environment }.returns(Environment.PRODUCTION)
        coEvery { configurationDataSource.get() }.returns(configurationData)

        runTest {
            val preAuthConfiguration = repository.getPreAuthConfiguration(supportedProtocolVersions).getOrThrow()
            assertEquals(
                listOf(ProtocolVersion.V_210, ProtocolVersion.V_220, ProtocolVersion.V_231),
                preAuthConfiguration.protocolVersions,
            )
            assertEquals(Environment.PRODUCTION, preAuthConfiguration.environment)
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getPreAuthConfiguration(supportedProtocolVersions) }
    }

    @Test
    fun `getPreAuthConfiguration should return ThreeDsUnknownProtocolException when there is no 2_1_0, 2_2_0 or 2_3_1 in supported protocol versions`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)

        val supportedThreeDsProtocolVersions = listOf(UNSUPPORTED_PROTOCOL_VERSION)

        every { configurationData.environment }.returns(Environment.PRODUCTION)
        coEvery { configurationDataSource.get() }.returns(configurationData)

        runTest {
            val exception =
                requireNotNull(repository.getPreAuthConfiguration(supportedThreeDsProtocolVersions).exceptionOrNull())
            assertEquals(ThreeDsUnknownProtocolException::class.java, exception::class.java)
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getPreAuthConfiguration(supportedThreeDsProtocolVersions) }
    }

    @Test
    fun `getPreAuthConfiguration should throw MissingConfigurationException when there no configuration`() {
        val supportedThreeDsProtocolVersions = listOf(ProtocolVersion.V_210.versionNumber)

        every { configurationDataSource.get() }.throws(
            MissingConfigurationException(null),
        )
        runTest {
            val exception =
                requireNotNull(repository.getPreAuthConfiguration(supportedThreeDsProtocolVersions).exceptionOrNull())
            assertEquals(MissingConfigurationException::class.java, exception::class.java)
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getPreAuthConfiguration(supportedThreeDsProtocolVersions) }
    }

    @Test
    fun `getPreAuthConfiguration should return environment SANDBOX when environment is SANDBOX`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)

        val supportedProtocolVersions = listOf(ProtocolVersion.V_210.versionNumber)

        every { configurationData.environment }.returns(Environment.SANDBOX)
        coEvery { configurationDataSource.get() }.returns(configurationData)

        runTest {
            val preAuthConfiguration = repository.getPreAuthConfiguration(supportedProtocolVersions).getOrThrow()
            assertEquals(Environment.SANDBOX, preAuthConfiguration.environment)
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getPreAuthConfiguration(supportedProtocolVersions) }
    }

    @Test
    fun `getProtocolVersion should return V_220 when environment is STAGING`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)

        val supportedThreeDsProtocolVersions = listOf(ProtocolVersion.V_210.versionNumber)

        every { configurationData.environment }.returns(Environment.STAGING)
        coEvery { configurationDataSource.get() }.returns(configurationData)

        runTest {
            val preAuthConfiguration = repository.getPreAuthConfiguration(supportedThreeDsProtocolVersions).getOrThrow()
            assertEquals(Environment.STAGING, preAuthConfiguration.environment)
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getPreAuthConfiguration(supportedThreeDsProtocolVersions) }
    }

    @Test
    fun `getProtocolVersion should return V_220 when environment is DEV`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)

        val supportedThreeDsProtocolVersions = listOf(ProtocolVersion.V_220.versionNumber)

        every { configurationData.environment }.returns(Environment.DEV)
        coEvery { configurationDataSource.get() }.returns(configurationData)

        runTest {
            val preAuthConfiguration = repository.getPreAuthConfiguration(supportedThreeDsProtocolVersions).getOrThrow()
            assertEquals(Environment.DEV, preAuthConfiguration.environment)
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getPreAuthConfiguration(supportedThreeDsProtocolVersions) }
    }

    private companion object {
        const val UNSUPPORTED_PROTOCOL_VERSION = "2.3.2"
    }
}
