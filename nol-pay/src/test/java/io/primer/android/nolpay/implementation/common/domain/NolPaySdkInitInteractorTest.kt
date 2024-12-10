package io.primer.android.nolpay.implementation.common.domain

import com.snowballtech.transit.rta.configuration.TransitConfiguration
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.core.domain.None
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.nolpay.InstantExecutorExtension
import io.primer.android.nolpay.implementation.common.domain.model.NolPayConfiguration
import io.primer.android.nolpay.implementation.common.domain.repository.NolPayAppSecretRepository
import io.primer.android.nolpay.implementation.common.domain.repository.NolPaySdkInitConfigurationRepository
import io.primer.nolpay.api.PrimerNolPay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPaySdkInitInteractorTest {

    @RelaxedMockK
    lateinit var secretRepository: NolPayAppSecretRepository

    @RelaxedMockK
    lateinit var configurationRepository: NolPaySdkInitConfigurationRepository

    @RelaxedMockK
    lateinit var nolPay: PrimerNolPay

    @RelaxedMockK
    internal lateinit var logReporter: LogReporter

    private lateinit var interactor: NolPaySdkInitInteractor

    @BeforeEach
    fun setUp() {
        interactor = NolPaySdkInitInteractor(secretRepository, configurationRepository, nolPay, logReporter)
    }

    @Test
    fun `execute should return Unit when PrimerNolPay initSDK was successful`() {
        val params = mockk<None>(relaxed = true)
        val configuration = mockk<NolPayConfiguration>(relaxed = true)
        val transitConfiguration = mockk<TransitConfiguration>(relaxed = true)

        coEvery { configurationRepository.getConfiguration() }.returns(
            Result.success(configuration)
        )

        every { nolPay.initSDK(any(), any(), any(), any()) }.returns(transitConfiguration)

        runTest {
            val result = interactor(params)
            assert(result.isSuccess)
            assertEquals(Unit, result.getOrThrow())
        }

        coVerify { configurationRepository.getConfiguration() }
    }

    @Test
    fun `execute should return error when PrimerNolPay initSDK fails`() {
        val params = mockk<None>(relaxed = true)
        val expectedException = mockk<java.lang.Exception>(relaxed = true)
        val configuration = mockk<NolPayConfiguration>(relaxed = true)

        coEvery { nolPay.initSDK(any(), any(), any(), any()) }.throws(
            expectedException
        )

        coEvery { configurationRepository.getConfiguration() }.returns(
            Result.success(configuration)
        )

        assertThrows<java.lang.Exception> {
            runTest {
                val result = interactor(params)
                assert(result.isFailure)
                result.getOrThrow()
            }
        }

        coVerify { configurationRepository.getConfiguration() }
    }

    @Test
    fun `execute should return error when NolPayConfigurationRepository getConfiguration fails`() {
        val params = mockk<None>(relaxed = true)
        val expectedException = mockk<io.primer.android.errors.data.exception.IllegalValueException>(relaxed = true)

        coEvery { configurationRepository.getConfiguration() }.throws(
            expectedException
        )

        val exception = assertThrows<io.primer.android.errors.data.exception.IllegalValueException> {
            runTest {
                val result = interactor(params)
                assert(result.isFailure)
                result.getOrThrow()
            }
        }

        assertEquals(expectedException, exception)

        coVerify(exactly = 0) { secretRepository.getAppSecret(any(), any()) }
        coVerify { configurationRepository.getConfiguration() }
    }

    @Test
    fun `handler should return app secret key from server`() = runTest {
        val sdkId = "sdkId"
        val appId = "merchantAppId"
        val appSecret = "appSecret"

        coEvery {
            configurationRepository.getConfiguration()
        } returns Result.success(
            mockk {
                every { merchantAppId } returns appId
            }
        )

        coEvery { secretRepository.getAppSecret(sdkId, appId) }.returns(Result.success(appSecret))

        val result = interactor.handler.getAppSecretKeyFromServer(sdkId)

        assertEquals(appSecret, result)
    }

    @Test
    fun `handler should throw exception when secret repository fails`(): Unit = runTest {
        val sdkId = "sdkId"
        val appId = "merchantAppId"
        val exception = Exception("Error fetching app secret")

        coEvery {
            configurationRepository.getConfiguration()
        } returns Result.success(
            mockk {
                every { merchantAppId } returns appId
            }
        )
        coEvery { secretRepository.getAppSecret(sdkId, appId) } returns Result.failure(exception)

        assertFailsWith<Exception> {
            interactor.handler.getAppSecretKeyFromServer(sdkId)
        }
    }
}
