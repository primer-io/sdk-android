package io.primer.android.klarna.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataResponse
import io.primer.android.klarna.implementation.session.data.models.FinalizeKlarnaSessionDataResponse
import io.primer.android.klarna.implementation.session.data.models.KlarnaSessionData
import io.primer.android.klarna.implementation.session.domain.FinalizeKlarnaSessionInteractor
import io.primer.android.klarna.implementation.session.domain.KlarnaCustomerTokenInteractor
import io.primer.android.klarna.implementation.session.domain.models.KlarnaCustomerTokenParam
import io.primer.android.klarna.implementation.tokenization.domain.KlarnaTokenizationInteractor
import io.primer.android.klarna.implementation.tokenization.domain.model.KlarnaCheckoutPaymentInstrumentParams
import io.primer.android.klarna.implementation.tokenization.domain.model.KlarnaVaultPaymentInstrumentParams
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KlarnaTokenizationDelegateTest {
    private lateinit var klarnaCustomerTokenInteractor: KlarnaCustomerTokenInteractor
    private lateinit var finalizeKlarnaSessionInteractor: FinalizeKlarnaSessionInteractor
    private lateinit var tokenizationInteractor: KlarnaTokenizationInteractor
    private lateinit var klarnaSessionData: KlarnaSessionData
    private lateinit var delegate: KlarnaTokenizationDelegate

    @BeforeEach
    fun setUp() {
        klarnaCustomerTokenInteractor = mockk()
        finalizeKlarnaSessionInteractor = mockk()
        tokenizationInteractor = mockk()
        klarnaSessionData = mockk()
        delegate =
            KlarnaTokenizationDelegate(
                klarnaCustomerTokenInteractor,
                finalizeKlarnaSessionInteractor,
                tokenizationInteractor,
            )
    }

    @Test
    fun `mapTokenizationData should return KlarnaVaultPaymentInstrumentParams when session intent is VAULT`() =
        runBlocking {
            // Given
            val input =
                KlarnaTokenizationInputable(
                    sessionId = "session-id",
                    authorizationToken = "auth-token",
                    paymentMethodType = "klarna",
                    primerSessionIntent = PrimerSessionIntent.VAULT,
                )

            val customerTokenData =
                CreateCustomerTokenDataResponse(
                    customerTokenId = "customer-token-id",
                    sessionData = klarnaSessionData,
                )

            coEvery { klarnaCustomerTokenInteractor.invoke(any()) } returns Result.success(customerTokenData)

            // When
            val result = delegate.mapTokenizationData(input)

            // Then
            assert(result.isSuccess)
            val tokenizationParams = result.getOrThrow()
            val expectedParams =
                KlarnaVaultPaymentInstrumentParams(
                    klarnaCustomerToken = "customer-token-id",
                    sessionData = klarnaSessionData,
                )
            assertEquals(expectedParams, tokenizationParams.paymentInstrumentParams)
            assertEquals(PrimerSessionIntent.VAULT, tokenizationParams.sessionIntent)
            coVerify { klarnaCustomerTokenInteractor.invoke(KlarnaCustomerTokenParam("session-id", "auth-token")) }
        }

    @Test
    fun `mapTokenizationData should return KlarnaCheckoutPaymentInstrumentParams when session intent is CHECKOUT`() =
        runBlocking {
            // Given
            val input =
                KlarnaTokenizationInputable(
                    sessionId = "session-id",
                    authorizationToken = "auth-token",
                    paymentMethodType = "klarna",
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                )

            val customerTokenData =
                FinalizeKlarnaSessionDataResponse(
                    sessionData = klarnaSessionData,
                )

            coEvery { finalizeKlarnaSessionInteractor.invoke(any()) } returns Result.success(customerTokenData)

            // When
            val result = delegate.mapTokenizationData(input)

            // Then
            assert(result.isSuccess)
            val tokenizationParams = result.getOrThrow()
            val expectedParams =
                KlarnaCheckoutPaymentInstrumentParams(
                    klarnaAuthorizationToken = "auth-token",
                    sessionData = klarnaSessionData,
                )
            assertEquals(expectedParams, tokenizationParams.paymentInstrumentParams)
            assertEquals(PrimerSessionIntent.CHECKOUT, tokenizationParams.sessionIntent)
            coVerify { finalizeKlarnaSessionInteractor.invoke(KlarnaCustomerTokenParam("session-id", "auth-token")) }
        }
}
