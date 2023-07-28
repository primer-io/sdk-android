package io.primer.android.threeds.data.repository

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.threeds.data.datasource.Remote3DSAuthDataSource
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.data.models.postAuth.PostAuthResponse
import io.primer.android.threeds.domain.models.SuccessThreeDsContinueAuthParams
import io.primer.android.threeds.domain.models.ThreeDsVaultParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ThreeDsDataRepositoryTest {

    @RelaxedMockK
    internal lateinit var remote3DSAuthDataSource: Remote3DSAuthDataSource

    @RelaxedMockK
    internal lateinit var configurationDataSource: LocalConfigurationDataSource

    private lateinit var repository: ThreeDsDataRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        repository = ThreeDsDataRepository(remote3DSAuthDataSource, configurationDataSource)
    }

    @Test
    fun `begin3DSAuth should return correct BeginAuthResponse when there is local cached configuration and get3dsAuthToken is successful`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)
        coEvery { configurationDataSource.get() }.returns(flowOf(configurationData))

        val beginAuthResponseMock = mockk<BeginAuthResponse>(relaxed = true)
        coEvery { remote3DSAuthDataSource.get3dsAuthToken(any(), any(), any()) }.returns(
            flowOf(
                beginAuthResponseMock
            )
        )

        runTest {
            val beginAuthResponse =
                repository.begin3DSAuth("", mockk<ThreeDsVaultParams>(relaxed = true)).first()
            assertEquals(beginAuthResponseMock, beginAuthResponse)
        }

        coVerify { configurationDataSource.get() }
        coVerify { remote3DSAuthDataSource.get3dsAuthToken(any(), any(), any()) }
    }

    @Test
    fun `continue3DSAuth should return correct PostAuthResponse when there is local cached configuration and continue3dsAuth is successful`() {
        val configurationData = mockk<ConfigurationData>(relaxed = true)
        coEvery { configurationDataSource.get() }.returns(flowOf(configurationData))

        val continueAuthResponseMock = mockk<PostAuthResponse>(relaxed = true)
        coEvery { remote3DSAuthDataSource.continue3dsAuth(any(), any(), any()) }.returns(
            flowOf(
                continueAuthResponseMock
            )
        )

        runTest {
            val continueAuthResponse =
                repository.continue3DSAuth(
                    "",
                    SuccessThreeDsContinueAuthParams("1.0.0", "2.1.0")
                )
                    .first()
            assertEquals(continueAuthResponseMock, continueAuthResponse)
        }

        coVerify { configurationDataSource.get() }
        coVerify { remote3DSAuthDataSource.continue3dsAuth(any(), any(), any()) }
    }
}
