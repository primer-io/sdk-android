package io.primer.android.components.domain.payments.paymentMethods.nolPay

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPaySecretParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayAppSecretRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayConfigurationRepository
import io.primer.android.data.base.exceptions.IllegalValueException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayAppSecretInteractorTest {

    @RelaxedMockK
    lateinit var secretRepository: NolPayAppSecretRepository

    @RelaxedMockK
    lateinit var configurationRepository: NolPayConfigurationRepository

    private lateinit var interactor: NolPayAppSecretInteractor

    @BeforeEach
    fun setUp() {
        interactor = NolPayAppSecretInteractor(
            secretRepository,
            configurationRepository,
        )
    }

    @Test
    fun `execute should return NolPay secret when NolPayAppSecretRepository getAppSecret returns secret data`() {
        val params = mockk<NolPaySecretParams>(relaxed = true)
        val configuration = mockk<NolPayConfiguration>(relaxed = true)

        coEvery { secretRepository.getAppSecret(any(), any()) }.returns(
            Result.success("secret")
        )

        coEvery { configurationRepository.getConfiguration() }.returns(flowOf(configuration))

        runTest {
            val result = interactor(params)
            assert(result.isSuccess)
            assertEquals("secret", result.getOrThrow())
        }

        coVerify { secretRepository.getAppSecret(any(), any()) }
        coVerify { configurationRepository.getConfiguration() }
    }

    @Test
    fun `execute should return NolPay secret when NolPayAppSecretRepository getAppSecret fails`() {
        val params = mockk<NolPaySecretParams>(relaxed = true)
        val expectedException = mockk<Exception>(relaxed = true)
        val configuration = mockk<NolPayConfiguration>(relaxed = true)

        coEvery { secretRepository.getAppSecret(any(), any()) }.returns(
            Result.failure(expectedException)
        )

        coEvery { configurationRepository.getConfiguration() }.returns(flowOf(configuration))

        val exception = assertThrows<Exception> {
            runTest {
                val result = interactor(params)
                assert(result.isFailure)
                result.getOrThrow()
            }
        }

        assertEquals(expectedException, exception)

        coVerify { secretRepository.getAppSecret(any(), any()) }
        coVerify { configurationRepository.getConfiguration() }
    }

    @Test
    fun `execute should return NolPay secret when NolPayConfigurationRepository getConfiguration fails and getAppSecret fails`() {
        val params = mockk<NolPaySecretParams>(relaxed = true)
        val expectedAppSecretException = mockk<Exception>(relaxed = true)
        val expectedException = mockk<IllegalValueException>(relaxed = true)

        coEvery { secretRepository.getAppSecret(any(), any()) }.returns(
            Result.failure(expectedAppSecretException)
        )

        coEvery { configurationRepository.getConfiguration() }.returns(
            flow {
                throw expectedException
            }
        )

        val exception = assertThrows<IllegalValueException> {
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
    fun `execute should return NolPay secret when NolPayConfigurationRepository getConfiguration fails`() {
        val params = mockk<NolPaySecretParams>(relaxed = true)
        val expectedException = mockk<IllegalValueException>(relaxed = true)

        coEvery { secretRepository.getAppSecret(any(), any()) }.returns(
            Result.success("secret")
        )

        coEvery { configurationRepository.getConfiguration() }.returns(
            flow {
                throw expectedException
            }
        )

        val exception = assertThrows<IllegalValueException> {
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
}
