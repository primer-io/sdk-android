package io.primer.android.phoneNumber.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.phoneNumber.PrimerPhoneNumberData
import io.primer.android.phoneNumber.implementation.configuration.domain.PhoneNumberConfigurationInteractor
import io.primer.android.phoneNumber.implementation.configuration.domain.model.PhoneNumberConfig
import io.primer.android.phoneNumber.implementation.configuration.domain.model.PhoneNumberConfigParams
import io.primer.android.phoneNumber.implementation.tokenization.domain.PhoneNumberTokenizationInteractor
import io.primer.android.phoneNumber.implementation.tokenization.domain.model.PhoneNumberPaymentInstrumentParams
import io.primer.android.phoneNumber.implementation.tokenization.presentation.composable.PhoneNumberTokenizationInputable
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PhoneNumberTokenizationDelegateTest {
    private val configurationInteractor = mockk<PhoneNumberConfigurationInteractor>()
    private val tokenizationInteractor = mockk<PhoneNumberTokenizationInteractor>()
    private val delegate = PhoneNumberTokenizationDelegate(configurationInteractor, tokenizationInteractor)

    @Test
    fun `mapTokenizationData should return tokenization params successfully`() =
        runBlocking {
            val input =
                PhoneNumberTokenizationInputable(
                    paymentMethodType = "phoneNumber",
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    phoneNumberData = PrimerPhoneNumberData("1234567890"),
                )

            val configParams = PhoneNumberConfigParams(paymentMethodType = input.paymentMethodType)
            val config = PhoneNumberConfig(paymentMethodConfigId = "configId", locale = "en-US")

            coEvery { configurationInteractor.invoke(configParams) } returns Result.success(config)

            val result = delegate.mapTokenizationData(input)

            val expected =
                TokenizationParams(
                    paymentInstrumentParams =
                    PhoneNumberPaymentInstrumentParams(
                        paymentMethodType = input.paymentMethodType,
                        paymentMethodConfigId = config.paymentMethodConfigId,
                        locale = config.locale,
                        phoneNumber = input.phoneNumberData.phoneNumber,
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
                PhoneNumberTokenizationInputable(
                    paymentMethodType = "phoneNumber",
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    phoneNumberData = PrimerPhoneNumberData("1234567890"),
                )

            val configParams = PhoneNumberConfigParams(paymentMethodType = input.paymentMethodType)
            val error = Exception("Configuration error")

            coEvery { configurationInteractor.invoke(configParams) } returns Result.failure(error)

            val result = delegate.mapTokenizationData(input)

            assertEquals(Result.failure<PhoneNumberPaymentInstrumentParams>(error), result)
            coVerify { configurationInteractor.invoke(configParams) }
        }
}
