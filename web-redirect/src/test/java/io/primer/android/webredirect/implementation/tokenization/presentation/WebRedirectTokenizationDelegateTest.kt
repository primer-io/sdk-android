package io.primer.android.webredirect.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.domain.None
import io.primer.android.webRedirectShared.implementation.deeplink.domain.RedirectDeeplinkInteractor
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.webredirect.implementation.configuration.domain.WebRedirectConfigurationInteractor
import io.primer.android.webredirect.implementation.configuration.domain.model.WebRedirectConfig
import io.primer.android.webredirect.implementation.configuration.domain.model.WebRedirectConfigParams
import io.primer.android.webredirect.implementation.tokenization.domain.WebRedirectTokenizationInteractor
import io.primer.android.webredirect.implementation.tokenization.domain.platform.PlatformResolver
import io.primer.android.webredirect.implementation.tokenization.presentation.model.WebRedirectTokenizationInputable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class)
internal class WebRedirectTokenizationDelegateTest {
    private lateinit var configurationInteractor: WebRedirectConfigurationInteractor
    private lateinit var tokenizationInteractor: WebRedirectTokenizationInteractor
    private lateinit var deeplinkInteractor: RedirectDeeplinkInteractor
    private lateinit var platformResolver: PlatformResolver
    private lateinit var delegate: WebRedirectTokenizationDelegate
    private val input =
        WebRedirectTokenizationInputable(
            paymentMethodType = "REDIRECT",
            primerSessionIntent = PrimerSessionIntent.CHECKOUT,
        )
    private val webRedirectConfigParams = WebRedirectConfigParams(paymentMethodType = input.paymentMethodType)
    private val webRedirectConfig =
        WebRedirectConfig(
            paymentMethodConfigId = "web-redirect",
            locale = "en",
        )

    @BeforeEach
    fun setUp() {
        configurationInteractor = mockk()
        tokenizationInteractor = mockk()
        deeplinkInteractor = mockk()
        platformResolver = mockk()
        delegate =
            WebRedirectTokenizationDelegate(
                configurationInteractor,
                tokenizationInteractor,
                deeplinkInteractor,
                platformResolver,
            )
    }

    @Test
    fun `mapTokenizationData returns success result when the configurationInteractor returns Result success`() =
        runTest {
            every { deeplinkInteractor(None) } returns "https://example.com"
            every {
                platformResolver.getPlatform(paymentMethodType = input.paymentMethodType)
            } returns PlatformResolver.ANDROID_PLATFORM
            coEvery { configurationInteractor(webRedirectConfigParams) } returns Result.success(webRedirectConfig)

            val result = delegate.mapTokenizationData(input)

            assertTrue(result.isSuccess)
            val tokenizationParams = result.getOrNull()!!
            val paymentInstrumentParams = tokenizationParams.paymentInstrumentParams

            assertEquals(input.paymentMethodType, paymentInstrumentParams.paymentMethodType)
            coVerify { configurationInteractor(webRedirectConfigParams) }
        }

    @Test
    fun `mapTokenizationData returns failure result when the configurationInteractor returns Result failure`() =
        runTest {
            val exception = Exception("Configuration error")
            coEvery { configurationInteractor(webRedirectConfigParams) } returns Result.failure(exception)

            val result = delegate.mapTokenizationData(input)

            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())

            coVerify { configurationInteractor(webRedirectConfigParams) }
        }
}
