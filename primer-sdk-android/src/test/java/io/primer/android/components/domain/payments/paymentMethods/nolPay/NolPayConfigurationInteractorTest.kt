package io.primer.android.components.domain.payments.paymentMethods.nolPay

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayConfigurationRepository
import io.primer.android.domain.base.None
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayConfigurationInteractorTest {

    @RelaxedMockK
    lateinit var configurationRepository: NolPayConfigurationRepository

    private lateinit var interactor: NolPayConfigurationInteractor

    @BeforeEach
    fun setUp() {
        interactor = NolPayConfigurationInteractor(configurationRepository)
    }

    @Test
    fun `execute should return NolPayConfiguration when NolPayConfigurationRepository getConfiguration is successful`() {
        val configuration = mockk<NolPayConfiguration>(relaxed = true)
        coEvery { configurationRepository.getConfiguration() }.returns(flowOf(configuration))

        runTest {
            val result = interactor(None()).first()
            assertEquals(configuration, result)
        }
    }

    @Test
    fun `execute should return exception when NolPayConfigurationRepository getConfiguration failed`() {
        val expectedException = mockk<Exception>(relaxed = true)
        every { configurationRepository.getConfiguration() } throws expectedException

        val exception = assertThrows<Exception> {
            runTest {
                val result = interactor(None())
            }
        }

        assertEquals(expectedException, exception)
    }
}
