package io.primer.android.threeds.data.repository

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.exception.MissingConfigurationException
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.data.configuration.models.Environment
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
    internal lateinit var configurationDataSource: LocalConfigurationDataSource

    private lateinit var repository: ThreeDsConfigurationDataRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        repository = ThreeDsConfigurationDataRepository(configurationDataSource)
    }

    @Test
    fun `getConfiguration should return correct ThreeDsKeysParams when there is local cached configuration`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)
        coEvery { configurationDataSource.get() }.returns(flowOf(configurationData))

        runTest {
            val threeDsParams = repository.getConfiguration().first()
            assertEquals(configurationData.environment, threeDsParams.environment)
            assertEquals(configurationData.keys?.netceteraLicenseKey, threeDsParams.licenceKey)
            assertEquals(
                configurationData.keys?.threeDSecureIoCertificates,
                threeDsParams.threeDsCertificates
            )
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getConfiguration() }
    }

    @Test
    fun `getConfiguration should return MissingConfigurationException when there is no local cached configuration`() {
        every { configurationDataSource.get() }.throws(
            MissingConfigurationException(null)
        )
        assertThrows<MissingConfigurationException> {
            runTest {
                repository.getConfiguration().first()
            }
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getConfiguration() }
    }

    @Test
    fun `getPreAuthConfiguration should return V_210 when environment is PRODUCTION`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)
        every { configurationData.environment }.returns(Environment.PRODUCTION)
        coEvery { configurationDataSource.get() }.returns(flowOf(configurationData))

        runTest {
            val preAuthConfiguration = repository.getPreAuthConfiguration().first()
            assertEquals(ProtocolVersion.V_210, preAuthConfiguration.protocolVersion)
            assertEquals(Environment.PRODUCTION, preAuthConfiguration.environment)
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getPreAuthConfiguration() }
    }

    @Test
    fun `getProtocolVersion should return V_220 when environment is SANDBOX`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)
        every { configurationData.environment }.returns(Environment.SANDBOX)
        coEvery { configurationDataSource.get() }.returns(flowOf(configurationData))

        runTest {
            val preAuthConfiguration = repository.getPreAuthConfiguration().first()
            assertEquals(ProtocolVersion.V_220, preAuthConfiguration.protocolVersion)
            assertEquals(Environment.SANDBOX, preAuthConfiguration.environment)
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getPreAuthConfiguration() }
    }

    @Test
    fun `getProtocolVersion should return V_220 when environment is STAGING`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)
        every { configurationData.environment }.returns(Environment.STAGING)
        coEvery { configurationDataSource.get() }.returns(flowOf(configurationData))

        runTest {
            val preAuthConfiguration = repository.getPreAuthConfiguration().first()
            assertEquals(ProtocolVersion.V_220, preAuthConfiguration.protocolVersion)
            assertEquals(Environment.STAGING, preAuthConfiguration.environment)
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getPreAuthConfiguration() }
    }

    @Test
    fun `getProtocolVersion should return V_220 when environment is DEV`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)
        every { configurationData.environment }.returns(Environment.DEV)
        coEvery { configurationDataSource.get() }.returns(flowOf(configurationData))

        runTest {
            val preAuthConfiguration = repository.getPreAuthConfiguration().first()
            assertEquals(ProtocolVersion.V_220, preAuthConfiguration.protocolVersion)
            assertEquals(Environment.DEV, preAuthConfiguration.environment)
        }

        coVerify { configurationDataSource.get() }
        coVerify { repository.getPreAuthConfiguration() }
    }
}
