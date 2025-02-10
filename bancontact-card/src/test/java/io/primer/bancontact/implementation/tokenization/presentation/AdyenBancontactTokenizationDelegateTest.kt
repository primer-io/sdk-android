package io.primer.bancontact.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.bancontact.PrimerBancontactCardData
import io.primer.android.bancontact.implementation.configuration.domain.AdyenBancontactConfigurationInteractor
import io.primer.android.bancontact.implementation.configuration.domain.model.AdyenBancontactConfig
import io.primer.android.bancontact.implementation.configuration.domain.model.AdyenBancontactConfigParams
import io.primer.android.bancontact.implementation.tokenization.domain.AdyenBancontactTokenizationInteractor
import io.primer.android.bancontact.implementation.tokenization.presentation.AdyenBancontactTokenizationDelegate
import io.primer.android.bancontact.implementation.tokenization.presentation.composable.AdyenBancontactTokenizationInputable
import io.primer.android.core.domain.None
import io.primer.android.webRedirectShared.implementation.deeplink.domain.RedirectDeeplinkInteractor
import io.primer.android.core.InstantExecutorExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class)
class AdyenBancontactTokenizationDelegateTest {
    private lateinit var configurationInteractor: AdyenBancontactConfigurationInteractor
    private lateinit var tokenizationInteractor: AdyenBancontactTokenizationInteractor
    private lateinit var deeplinkInteractor: RedirectDeeplinkInteractor
    private lateinit var delegate: AdyenBancontactTokenizationDelegate
    private val input =
        AdyenBancontactTokenizationInputable(
            paymentMethodType = "AdyenBancontact",
            primerSessionIntent = PrimerSessionIntent.CHECKOUT,
            cardData =
            PrimerBancontactCardData(
                cardNumber = "4111111111111111",
                expiryDate = "12/25",
                cardHolderName = "John Doe",
            ),
        )
    private val adyenBancontactConfigParams = AdyenBancontactConfigParams(paymentMethodType = input.paymentMethodType)
    private val adyenBancontactConfig =
        AdyenBancontactConfig(
            paymentMethodConfigId = "AdyenBancontact",
            locale = Locale.US,
        )

    @BeforeEach
    fun setUp() {
        configurationInteractor = mockk()
        tokenizationInteractor = mockk()
        deeplinkInteractor = mockk()
        delegate =
            AdyenBancontactTokenizationDelegate(
                configurationInteractor,
                deeplinkInteractor,
                tokenizationInteractor,
            )
    }

    @Test
    fun `mapTokenizationData returns success result when the configurationInteractor returns Result success`() =
        runTest {
            every { deeplinkInteractor(None) } returns "https://example.com"
            coEvery { configurationInteractor(adyenBancontactConfigParams) } returns
                Result.success(adyenBancontactConfig)

            val result = delegate.mapTokenizationData(input)

            assertTrue(result.isSuccess)
            val tokenizationParams = result.getOrNull()!!
            val paymentInstrumentParams = tokenizationParams.paymentInstrumentParams

            assertEquals(input.paymentMethodType, paymentInstrumentParams.paymentMethodType)
            coVerify { configurationInteractor(adyenBancontactConfigParams) }
        }

    @Test
    fun `mapTokenizationData returns failure result when the configurationInteractor returns Result failure`() =
        runTest {
            val exception = Exception("Configuration error")
            coEvery { configurationInteractor(adyenBancontactConfigParams) } returns Result.failure(exception)

            val result = delegate.mapTokenizationData(input)

            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())

            coVerify { configurationInteractor(adyenBancontactConfigParams) }
        }
}
