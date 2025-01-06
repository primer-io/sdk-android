package io.primer.android.nolpay.implementation.paymentCard.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.PrimerSessionIntent
import io.primer.android.nolpay.implementation.configuration.domain.NolPayConfigurationInteractor
import io.primer.android.nolpay.implementation.configuration.domain.model.NolPayConfig
import io.primer.android.nolpay.implementation.configuration.domain.model.NolPayConfigParams
import io.primer.android.nolpay.implementation.paymentCard.tokenization.domain.NolPayTokenizationInteractor
import io.primer.android.nolpay.implementation.paymentCard.tokenization.domain.model.NolPayPaymentInstrumentParams
import io.primer.android.nolpay.implementation.paymentCard.tokenization.presentation.composable.NolPayTokenizationInputable
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.phoneMetadata.domain.PhoneMetadataInteractor
import io.primer.android.phoneMetadata.domain.model.PhoneMetadata
import io.primer.android.phoneMetadata.domain.model.PhoneMetadataParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
internal class NolPayTokenizationDelegateTest {
    @MockK
    lateinit var phoneMetadataInteractor: PhoneMetadataInteractor

    @MockK
    lateinit var configurationInteractor: NolPayConfigurationInteractor

    @MockK
    lateinit var tokenizationInteractor: NolPayTokenizationInteractor

    private lateinit var delegate: NolPayTokenizationDelegate

    @BeforeEach
    fun setUp() {
        delegate =
            NolPayTokenizationDelegate(
                phoneMetadataInteractor,
                configurationInteractor,
                tokenizationInteractor,
            )
    }

    @Test
    fun `mapTokenizationData should return TokenizationParams when successful`() =
        runTest {
            // Given
            val input =
                NolPayTokenizationInputable(
                    paymentMethodType = "NOL_PAY",
                    mobileNumber = "1234567890",
                    nolPayCardNumber = "1234",
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                )

            val configuration =
                NolPayConfig(
                    paymentMethodConfigId = "configId",
                    locale = "en-US",
                )

            val phoneMetadata =
                PhoneMetadata(
                    countryCode = "1",
                    nationalNumber = "1234567890",
                )

            coEvery { configurationInteractor(any<NolPayConfigParams>()) } returns Result.success(configuration)
            coEvery { phoneMetadataInteractor(any<PhoneMetadataParams>()) } returns Result.success(phoneMetadata)

            // When
            val result = delegate.mapTokenizationData(input)

            // Then
            assert(result.isSuccess)
            val tokenizationParams = result.getOrThrow()
            val expectedTokenizationParams =
                TokenizationParams(
                    NolPayPaymentInstrumentParams(
                        paymentMethodType = "NOL_PAY",
                        paymentMethodConfigId = "configId",
                        locale = "en-US",
                        mobileCountryCode = "1",
                        mobileNumber = "1234567890",
                        nolPayCardNumber = "1234",
                    ),
                    PrimerSessionIntent.CHECKOUT,
                )

            assertEquals(expectedTokenizationParams, tokenizationParams)

            coVerify { configurationInteractor(NolPayConfigParams(paymentMethodType = "NOL_PAY")) }
            coVerify { phoneMetadataInteractor(PhoneMetadataParams("1234567890")) }
        }

    @Test
    fun `mapTokenizationData should return failure when configurationInteractor fails`() =
        runTest {
            // Given
            val input =
                NolPayTokenizationInputable(
                    paymentMethodType = "NOL_PAY",
                    mobileNumber = "1234567890",
                    nolPayCardNumber = "1234",
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                )

            val exception = Exception("Configuration error")
            coEvery { configurationInteractor(any<NolPayConfigParams>()) } returns Result.failure(exception)
            coEvery { phoneMetadataInteractor(any<PhoneMetadataParams>()) } returns
                Result.success(
                    PhoneMetadata(
                        countryCode = "1",
                        nationalNumber = "1234567890",
                    ),
                )

            // When
            val result = delegate.mapTokenizationData(input)

            // Then
            assert(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())

            coVerify { configurationInteractor(NolPayConfigParams(paymentMethodType = "NOL_PAY")) }
            coVerify { phoneMetadataInteractor(PhoneMetadataParams("1234567890")) }
        }

    @Test
    fun `mapTokenizationData should return failure when phoneMetadataInteractor fails`() =
        runTest {
            // Given
            val input =
                NolPayTokenizationInputable(
                    paymentMethodType = "NOL_PAY",
                    mobileNumber = "1234567890",
                    nolPayCardNumber = "1234",
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                )

            val configuration =
                NolPayConfig(
                    paymentMethodConfigId = "configId",
                    locale = "en-US",
                )

            val exception = Exception("Phone metadata error")
            coEvery { configurationInteractor(any<NolPayConfigParams>()) } returns Result.success(configuration)
            coEvery { phoneMetadataInteractor(any<PhoneMetadataParams>()) } returns Result.failure(exception)

            // When
            val result = delegate.mapTokenizationData(input)

            // Then
            assert(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())

            coVerify { configurationInteractor(NolPayConfigParams(paymentMethodType = "NOL_PAY")) }
            coVerify { phoneMetadataInteractor(PhoneMetadataParams("1234567890")) }
        }
}
