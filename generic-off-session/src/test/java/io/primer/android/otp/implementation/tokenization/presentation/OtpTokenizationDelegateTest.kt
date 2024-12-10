package io.primer.android.otp.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.otp.PrimerOtpData
import io.primer.android.otp.implementation.configuration.domain.OtpConfigurationInteractor
import io.primer.android.otp.implementation.configuration.domain.model.OtpConfig
import io.primer.android.otp.implementation.configuration.domain.model.OtpConfigParams
import io.primer.android.otp.implementation.tokenization.domain.OtpTokenizationInteractor
import io.primer.android.otp.implementation.tokenization.domain.model.OtpPaymentInstrumentParams
import io.primer.android.otp.implementation.tokenization.presentation.composable.OtpTokenizationInputable
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OtpTokenizationDelegateTest {
    private val configurationInteractor = mockk<OtpConfigurationInteractor>()
    private val tokenizationInteractor = mockk<OtpTokenizationInteractor>()
    private val delegate = OtpTokenizationDelegate(configurationInteractor, tokenizationInteractor)

    @Test
    fun `mapTokenizationData should return tokenization params successfully`() = runBlocking {
        val input = OtpTokenizationInputable(
            paymentMethodType = "ADYEN_BLIK",
            primerSessionIntent = PrimerSessionIntent.CHECKOUT,
            otpData = PrimerOtpData("1234")
        )

        val configParams = OtpConfigParams(paymentMethodType = input.paymentMethodType)
        val config = OtpConfig(paymentMethodConfigId = "configId", locale = "en-US")

        coEvery { configurationInteractor.invoke(configParams) } returns Result.success(config)

        val result = delegate.mapTokenizationData(input)

        val expected = TokenizationParams(
            paymentInstrumentParams = OtpPaymentInstrumentParams(
                paymentMethodType = input.paymentMethodType,
                paymentMethodConfigId = config.paymentMethodConfigId,
                locale = config.locale,
                otp = input.otpData.otp
            ),
            sessionIntent = input.primerSessionIntent
        )

        assertEquals(Result.success(expected), result)
        coVerify { configurationInteractor.invoke(configParams) }
    }

    @Test
    fun `mapTokenizationData should return failure when configuration interactor fails`() = runBlocking {
        val input = OtpTokenizationInputable(
            paymentMethodType = "ADYEN_BLIK",
            primerSessionIntent = PrimerSessionIntent.CHECKOUT,
            otpData = PrimerOtpData("1234")
        )

        val configParams = OtpConfigParams(paymentMethodType = input.paymentMethodType)
        val error = Exception("Configuration error")

        coEvery { configurationInteractor.invoke(configParams) } returns Result.failure(error)

        val result = delegate.mapTokenizationData(input)

        assertEquals(Result.failure<OtpPaymentInstrumentParams>(error), result)
        coVerify { configurationInteractor.invoke(configParams) }
    }
}
