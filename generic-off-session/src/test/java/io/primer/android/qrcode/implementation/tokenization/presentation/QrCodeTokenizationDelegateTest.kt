package io.primer.android.qrcode.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.qrcode.implementation.configuration.domain.QrCodeConfigurationInteractor
import io.primer.android.qrcode.implementation.configuration.domain.model.QrCodeConfig
import io.primer.android.qrcode.implementation.configuration.domain.model.QrCodeConfigParams
import io.primer.android.qrcode.implementation.tokenization.domain.QrCodeTokenizationInteractor
import io.primer.android.qrcode.implementation.tokenization.domain.model.QrCodePaymentInstrumentParams
import io.primer.android.qrcode.implementation.tokenization.presentation.composable.QrCodeTokenizationInputable
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class QrCodeTokenizationDelegateTest {
    private val configurationInteractor = mockk<QrCodeConfigurationInteractor>()
    private val tokenizationInteractor = mockk<QrCodeTokenizationInteractor>()
    private val delegate = QrCodeTokenizationDelegate(configurationInteractor, tokenizationInteractor)

    @Test
    fun `mapTokenizationData should return tokenization params successfully`() =
        runBlocking {
            val input =
                QrCodeTokenizationInputable(
                    paymentMethodType = "qrCode",
                    primerSessionIntent = mockk(),
                )

            val configParams = QrCodeConfigParams(paymentMethodType = input.paymentMethodType)
            val config = QrCodeConfig(paymentMethodConfigId = "configId", locale = "en-US")

            coEvery { configurationInteractor.invoke(configParams) } returns Result.success(config)

            val result = delegate.mapTokenizationData(input)

            val expected =
                TokenizationParams(
                    paymentInstrumentParams =
                        QrCodePaymentInstrumentParams(
                            paymentMethodType = input.paymentMethodType,
                            paymentMethodConfigId = config.paymentMethodConfigId,
                            locale = config.locale,
                        ),
                    sessionIntent = input.primerSessionIntent,
                )

            assertEquals(Result.success(expected), result)
            coVerify { configurationInteractor.invoke(configParams) }
        }

    @Test
    fun `mapTokenizationData should return failure when configuration interactor fails`() =
        runBlocking {
            val input =
                QrCodeTokenizationInputable(
                    paymentMethodType = "qrCode",
                    primerSessionIntent = mockk(),
                )

            val configParams = QrCodeConfigParams(paymentMethodType = input.paymentMethodType)
            val error = Exception("Configuration error")

            coEvery { configurationInteractor.invoke(configParams) } returns Result.failure(error)

            val result = delegate.mapTokenizationData(input)

            assertEquals(Result.failure<QrCodePaymentInstrumentParams>(error), result)
            coVerify { configurationInteractor.invoke(configParams) }
        }
}
