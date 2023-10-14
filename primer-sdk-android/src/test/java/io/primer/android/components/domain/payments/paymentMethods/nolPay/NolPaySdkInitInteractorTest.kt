package io.primer.android.components.domain.payments.paymentMethods.nolPay

import com.snowballtech.transit.rta.configuration.TransitConfiguration
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPaySdkInitInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayAppSecretRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayConfigurationRepository
import io.primer.android.data.base.exceptions.IllegalValueException
import io.primer.android.domain.base.None
import io.primer.nolpay.api.PrimerNolPay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPaySdkInitInteractorTest {

    @RelaxedMockK
    lateinit var secretRepository: NolPayAppSecretRepository

    @RelaxedMockK
    lateinit var configurationRepository: NolPayConfigurationRepository

    @RelaxedMockK
    lateinit var nolPay: PrimerNolPay

    private lateinit var interactor: NolPaySdkInitInteractor

    @BeforeEach
    fun setUp() {
        interactor = NolPaySdkInitInteractor(
            secretRepository,
            configurationRepository,
            nolPay
        )
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
        val expectedException = mockk<IllegalValueException>(relaxed = true)

        coEvery { configurationRepository.getConfiguration() }.throws(
            expectedException
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
