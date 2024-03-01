package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.FinalizeKlarnaSessionDataResponse
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.KlarnaSessionData
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.FinalizeKlarnaSessionInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaCustomerTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.klarna.KlarnaCheckoutPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.klarna.KlarnaVaultPaymentInstrumentParams
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class KlarnaTokenizationDelegateTest {
    @MockK
    private lateinit var tokenizationInteractor: TokenizationInteractor

    @MockK
    private lateinit var klarnaCustomerTokenInteractor: KlarnaCustomerTokenInteractor

    @MockK
    private lateinit var finalizeKlarnaSessionInteractor: FinalizeKlarnaSessionInteractor

    private lateinit var delegate: KlarnaTokenizationDelegate

    @BeforeEach
    fun setUp() {
        delegate = KlarnaTokenizationDelegate(
            tokenizationInteractor = tokenizationInteractor,
            klarnaCustomerTokenInteractor = klarnaCustomerTokenInteractor,
            finalizeKlarnaSessionInteractor = finalizeKlarnaSessionInteractor
        )
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(
            klarnaCustomerTokenInteractor,
            tokenizationInteractor
        )
    }

    @Test
    fun `tokenize() should return Unit when session intent is VAULT and interactors succeed`() = runTest {
        val sessionId = "sessionId"
        val authorizationToken = "authorizationToken"
        val sessionData = mockk<KlarnaSessionData>()
        val customerTokenData = mockk<CreateCustomerTokenDataResponse>() {
            every { this@mockk.sessionData } returns sessionData
            every { customerTokenId } returns "customerTokenId"
        }
        coEvery {
            klarnaCustomerTokenInteractor.invoke(any())
        } returns Result.success(customerTokenData)
        every { tokenizationInteractor.executeV2(any()) } returns emptyFlow()

        val result = delegate.tokenize(sessionId, authorizationToken, PrimerSessionIntent.VAULT)

        assertSame(Unit, result.getOrThrow())
        coVerify(exactly = 1) {
            klarnaCustomerTokenInteractor.invoke(
                KlarnaCustomerTokenParam(
                    sessionId = sessionId,
                    authorizationToken = authorizationToken
                )
            )
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    paymentInstrumentParams = KlarnaVaultPaymentInstrumentParams(
                        klarnaCustomerToken = "customerTokenId",
                        sessionData = sessionData
                    ),
                    paymentMethodIntent = PrimerSessionIntent.VAULT
                )
            )
        }
    }

    @Test
    fun `tokenize() should return Unit when session intent is CHECKOUT and interactors succeed`() = runTest {
        val sessionId = "sessionId"
        val authorizationToken = "authorizationToken"
        val sessionData = mockk<KlarnaSessionData>()
        val data = mockk<FinalizeKlarnaSessionDataResponse>() {
            every { this@mockk.sessionData } returns sessionData
        }
        coEvery {
            finalizeKlarnaSessionInteractor.invoke(any())
        } returns Result.success(data)
        every { tokenizationInteractor.executeV2(any()) } returns emptyFlow()

        val result = delegate.tokenize(sessionId, authorizationToken, PrimerSessionIntent.CHECKOUT)

        assertSame(Unit, result.getOrThrow())
        coVerify(exactly = 1) {
            finalizeKlarnaSessionInteractor.invoke(
                KlarnaCustomerTokenParam(
                    sessionId = sessionId,
                    authorizationToken = authorizationToken
                )
            )
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    paymentInstrumentParams = KlarnaCheckoutPaymentInstrumentParams(
                        klarnaAuthorizationToken = authorizationToken,
                        sessionData = sessionData
                    ),
                    paymentMethodIntent = PrimerSessionIntent.CHECKOUT
                )
            )
        }
    }

    @Test
    fun `tokenize() should return exception when session intent is VAULT and the klarna customer token interactor fails`() = runTest {
        val exception = Exception()
        coEvery {
            klarnaCustomerTokenInteractor.invoke(any())
        } throws exception

        val result = delegate.tokenize("sessionId", "authorizationToken", PrimerSessionIntent.VAULT)

        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            klarnaCustomerTokenInteractor.invoke(any())
        }
    }

    @Test
    fun `tokenize() should return exception when session intent is CHECKOUT and the session finalization interactor fails`() = runTest {
        val exception = Exception()
        coEvery {
            finalizeKlarnaSessionInteractor.invoke(any())
        } returns Result.failure(exception)

        val result =
            delegate.tokenize("sessionId", "authorizationToken", PrimerSessionIntent.CHECKOUT)

        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            finalizeKlarnaSessionInteractor.invoke(any())
        }
    }

    @Test
    fun `tokenize() should return exception when session intent is VAULT and the tokenization interactor fails`() = runTest {
        val exception = Exception()
        val sessionId = "sessionId"
        val authorizationToken = "authorizationToken"
        val sessionData = mockk<KlarnaSessionData>()
        val customerTokenData = mockk<CreateCustomerTokenDataResponse>() {
            every { this@mockk.sessionData } returns sessionData
            every { customerTokenId } returns "customerTokenId"
        }
        coEvery {
            klarnaCustomerTokenInteractor.invoke(any())
        } returns Result.success(customerTokenData)
        every { tokenizationInteractor.executeV2(any()) } returns flow { throw exception }

        val result = delegate.tokenize(sessionId, authorizationToken, PrimerSessionIntent.VAULT)

        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            klarnaCustomerTokenInteractor.invoke(
                KlarnaCustomerTokenParam(
                    sessionId = sessionId,
                    authorizationToken = authorizationToken
                )
            )
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    paymentInstrumentParams = KlarnaVaultPaymentInstrumentParams(
                        klarnaCustomerToken = "customerTokenId",
                        sessionData = sessionData
                    ),
                    paymentMethodIntent = PrimerSessionIntent.VAULT
                )
            )
        }
    }

    @Test
    fun `tokenize() should return exception when session intent is CHECKOUT and the tokenization interactor fails`() = runTest {
        val exception = Exception()
        val sessionId = "sessionId"
        val authorizationToken = "authorizationToken"
        val sessionData = mockk<KlarnaSessionData>()
        val finalizationData = mockk<FinalizeKlarnaSessionDataResponse>() {
            every { this@mockk.sessionData } returns sessionData
        }
        coEvery {
            finalizeKlarnaSessionInteractor.invoke(any())
        } returns Result.success(finalizationData)
        every { tokenizationInteractor.executeV2(any()) } returns flow { throw exception }

        val result = delegate.tokenize(sessionId, authorizationToken, PrimerSessionIntent.CHECKOUT)

        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            finalizeKlarnaSessionInteractor.invoke(
                KlarnaCustomerTokenParam(
                    sessionId = sessionId,
                    authorizationToken = authorizationToken
                )
            )
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    paymentInstrumentParams = KlarnaCheckoutPaymentInstrumentParams(
                        klarnaAuthorizationToken = authorizationToken,
                        sessionData = sessionData
                    ),
                    paymentMethodIntent = PrimerSessionIntent.CHECKOUT
                )
            )
        }
    }
}
