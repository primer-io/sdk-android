package io.primer.android.ipay88.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.ipay88.InstantExecutorExtension
import io.primer.android.ipay88.implementation.configuration.domain.IPay88ConfigurationInteractor
import io.primer.android.ipay88.implementation.configuration.domain.model.IPay88Config
import io.primer.android.ipay88.implementation.configuration.domain.model.IPay88ConfigParams
import io.primer.android.ipay88.implementation.tokenization.domain.IPay88TokenizationInteractor
import io.primer.android.ipay88.implementation.tokenization.presentation.model.IPay88TokenizationInputable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class)
class IPay88TokenizationDelegateTest {

    private lateinit var configurationInteractor: IPay88ConfigurationInteractor
    private lateinit var tokenizationInteractor: IPay88TokenizationInteractor
    private lateinit var delegate: IPay88TokenizationDelegate
    private val input = IPay88TokenizationInputable(
        paymentMethodType = "CARD",
        primerSessionIntent = PrimerSessionIntent.CHECKOUT
    )
    private val iPay88ConfigParams = IPay88ConfigParams(paymentMethodType = input.paymentMethodType)

    private val iPay88Config = IPay88Config(
        paymentMethodConfigId = "web-redirect",
        locale = "en"
    )

    @BeforeEach
    fun setUp() {
        configurationInteractor = mockk()
        tokenizationInteractor = mockk()
        delegate = IPay88TokenizationDelegate(configurationInteractor, tokenizationInteractor)
    }

    @Test
    fun `mapTokenizationData returns success result when the configurationInteractor returns Result success`() = runTest {
        coEvery { configurationInteractor(iPay88ConfigParams) } returns Result.success(iPay88Config)

        val result = delegate.mapTokenizationData(input)

        assertTrue(result.isSuccess)
        val tokenizationParams = result.getOrNull()!!
        val paymentInstrumentParams = tokenizationParams.paymentInstrumentParams

        assertEquals(input.paymentMethodType, paymentInstrumentParams.paymentMethodType)
        coVerify { configurationInteractor(iPay88ConfigParams) }
    }

    @Test
    fun `mapTokenizationData returns failure result when the configurationInteractor returns Result failure`() = runTest {
        val exception = Exception("Configuration error")
        coEvery { configurationInteractor(iPay88ConfigParams) } returns Result.failure(exception)

        val result = delegate.mapTokenizationData(input)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        coVerify { configurationInteractor(iPay88ConfigParams) }
    }
}
