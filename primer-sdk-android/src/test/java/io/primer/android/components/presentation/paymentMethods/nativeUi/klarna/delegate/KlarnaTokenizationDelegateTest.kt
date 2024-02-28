package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaCustomerTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.klarna.KlarnaPaymentInstrumentParams
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class KlarnaTokenizationDelegateTest {
    private val primerSessionIntent = PrimerSessionIntent.CHECKOUT

    @MockK
    private lateinit var actionInteractor: ActionInteractor

    @MockK
    private lateinit var tokenizationInteractor: TokenizationInteractor

    @MockK
    private lateinit var klarnaCustomerTokenInteractor: KlarnaCustomerTokenInteractor

    @MockK
    private lateinit var primerConfig: PrimerConfig

    private lateinit var delegate: KlarnaTokenizationDelegate

    @BeforeEach
    fun setUp() {
        delegate = KlarnaTokenizationDelegate(
            actionInteractor = actionInteractor,
            tokenizationInteractor = tokenizationInteractor,
            klarnaCustomerTokenInteractor = klarnaCustomerTokenInteractor,
            primerConfig = primerConfig
        )
        every { primerConfig.paymentMethodIntent } returns primerSessionIntent
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(
            actionInteractor,
            klarnaCustomerTokenInteractor,
            primerConfig,
            tokenizationInteractor
        )
    }

    @Test
    fun `tokenize() should return Unit when interactors succeed`() = runTest {
        val sessionId = "sessionId"
        val authorizationToken = "authorizationToken"
        val sessionData = mockk<CreateCustomerTokenDataResponse.SessionData>()
        val customerTokenData = mockk<CreateCustomerTokenDataResponse>() {
            every { this@mockk.sessionData } returns sessionData
            every { customerTokenId } returns "customerTokenId"
        }
        every { actionInteractor(any()) } returns emptyFlow()
        every {
            klarnaCustomerTokenInteractor.execute(any())
        } returns flowOf(customerTokenData)
        every { tokenizationInteractor.executeV2(any()) } returns emptyFlow()

        val result = delegate.tokenize(sessionId, authorizationToken)

        assertSame(Unit, result.getOrThrow())
        coVerify(exactly = 1) {
            actionInteractor(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                    cardNetwork = null
                )
            )
            klarnaCustomerTokenInteractor.execute(
                KlarnaCustomerTokenParam(
                    sessionId = sessionId,
                    authorizationToken = authorizationToken
                )
            )
            primerConfig.paymentMethodIntent
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    paymentInstrumentParams = KlarnaPaymentInstrumentParams(
                        klarnaCustomerToken = "customerTokenId",
                        sessionData = sessionData
                    ),
                    paymentMethodIntent = primerSessionIntent
                )
            )
        }
    }

    @Test
    fun `tokenize() should return exception when the action interactor fails`() = runTest {
        val exception = Exception()
        every { actionInteractor(any()) } returns flow { throw exception }

        val result = delegate.tokenize("sessionId", "authorizationToken")

        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            actionInteractor(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                    cardNetwork = null
                )
            )
        }
    }

    @Test
    fun `tokenize() should return exception when the klarna customer token interactor fails`() = runTest {
        val exception = Exception()
        every { actionInteractor(any()) } returns emptyFlow()
        every {
            klarnaCustomerTokenInteractor.execute(any())
        } throws exception

        val result = delegate.tokenize("sessionId", "authorizationToken")

        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            actionInteractor(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                    cardNetwork = null
                )
            )
            klarnaCustomerTokenInteractor.execute(any())
        }
    }

    @Test
    fun `tokenize() should return exception when the tokenization interactor fails`() = runTest {
        val exception = Exception()
        val sessionId = "sessionId"
        val authorizationToken = "authorizationToken"
        val sessionData = mockk<CreateCustomerTokenDataResponse.SessionData>()
        val customerTokenData = mockk<CreateCustomerTokenDataResponse>() {
            every { this@mockk.sessionData } returns sessionData
            every { customerTokenId } returns "customerTokenId"
        }
        every { actionInteractor(any()) } returns emptyFlow()
        every {
            klarnaCustomerTokenInteractor.execute(any())
        } returns flowOf(customerTokenData)
        every { tokenizationInteractor.executeV2(any()) } returns flow { throw exception }

        val result = delegate.tokenize(sessionId, authorizationToken)

        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            actionInteractor(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                    cardNetwork = null
                )
            )
            klarnaCustomerTokenInteractor.execute(
                KlarnaCustomerTokenParam(
                    sessionId = sessionId,
                    authorizationToken = authorizationToken
                )
            )
            primerConfig.paymentMethodIntent
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    paymentInstrumentParams = KlarnaPaymentInstrumentParams(
                        klarnaCustomerToken = "customerTokenId",
                        sessionData = sessionData
                    ),
                    paymentMethodIntent = primerSessionIntent
                )
            )
        }
    }
}
