package io.primer.android.components.presentation.paymentMethods.componentWithRedirect.banks.delegate

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.deeplink.async.AsyncPaymentMethodDeeplinkInteractor
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.async.bankIssuer.BankIssuerPaymentInstrumentParams
import io.primer.android.payment.PaymentMethodDescriptor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class BankIssuerTokenizationDelegateTest {
    private val paymentMethodType: String = "paymentMethodType"

    private val paymentMethodConfigId = "paymentMethodConfigId"

    private val languageTag = "languageTag"

    private val issuerBankId = "issuerBankId"

    private val redirectUrl = "redirectUrl"

    private val primerSessionIntent = PrimerSessionIntent.CHECKOUT

    private val paymentMethodDescriptors = listOf(
        mockk<PaymentMethodDescriptor> {
            every { config.id } returns paymentMethodConfigId
            every { config.type } returns paymentMethodType
            every { localConfig.settings.locale.toLanguageTag() } returns languageTag
        }
    )

    @MockK
    private lateinit var actionInteractor: ActionInteractor

    @MockK
    private lateinit var tokenizationInteractor: TokenizationInteractor

    @MockK
    private lateinit var paymentMethodModulesInteractor: PaymentMethodModulesInteractor

    @RelaxedMockK
    private lateinit var asyncPaymentMethodDeeplinkInteractor: AsyncPaymentMethodDeeplinkInteractor

    @MockK
    private lateinit var primerConfig: PrimerConfig

    private lateinit var delegate: BankIssuerTokenizationDelegate

    @BeforeEach
    fun setUp() {
        delegate = BankIssuerTokenizationDelegate(
            paymentMethodType = paymentMethodType,
            actionInteractor = actionInteractor,
            tokenizationInteractor = tokenizationInteractor,
            paymentMethodModulesInteractor = paymentMethodModulesInteractor,
            asyncPaymentMethodDeeplinkInteractor = asyncPaymentMethodDeeplinkInteractor,
            primerConfig = primerConfig
        )
        every { primerConfig.paymentMethodIntent } returns primerSessionIntent
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(
            actionInteractor,
            paymentMethodModulesInteractor,
            asyncPaymentMethodDeeplinkInteractor,
            primerConfig,
            tokenizationInteractor
        )
    }

    @Test
    fun `tokenize() should return Unit when interactors succeed`() = runTest {
        every { actionInteractor(any()) } returns emptyFlow()
        every {
            paymentMethodModulesInteractor.getPaymentMethodDescriptors()
        } returns paymentMethodDescriptors
        every { asyncPaymentMethodDeeplinkInteractor(any()) } returns redirectUrl
        every { tokenizationInteractor.executeV2(any()) } returns emptyFlow()

        val result = delegate.tokenize(issuerBankId)

        assertSame(Unit, result.getOrThrow())
        coVerify(exactly = 1) {
            actionInteractor(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = paymentMethodType,
                    cardNetwork = null
                )
            )
            paymentMethodModulesInteractor.getPaymentMethodDescriptors()
            asyncPaymentMethodDeeplinkInteractor(any())
            primerConfig.paymentMethodIntent
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    paymentInstrumentParams = BankIssuerPaymentInstrumentParams(
                        paymentMethodType = paymentMethodType,
                        paymentMethodConfigId = paymentMethodConfigId,
                        locale = languageTag,
                        redirectionUrl = redirectUrl,
                        bankIssuer = issuerBankId
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

        val result = delegate.tokenize(issuerBankId)

        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            actionInteractor(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = paymentMethodType,
                    cardNetwork = null
                )
            )
        }
    }

    @Test
    fun `tokenize() should return exception when the payment method modules interactor fails`() = runTest {
        val exception = Exception()
        every { actionInteractor(any()) } returns emptyFlow()
        every {
            paymentMethodModulesInteractor.getPaymentMethodDescriptors()
        } throws exception

        val result = delegate.tokenize(issuerBankId)

        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            actionInteractor(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = paymentMethodType,
                    cardNetwork = null
                )
            )
            paymentMethodModulesInteractor.getPaymentMethodDescriptors()
        }
    }

    @Test
    fun `tokenize() should return exception when the deeplink interactor fails`() = runTest {
        val exception = Exception()
        every { actionInteractor(any()) } returns emptyFlow()
        every {
            paymentMethodModulesInteractor.getPaymentMethodDescriptors()
        } returns paymentMethodDescriptors
        every { asyncPaymentMethodDeeplinkInteractor(any()) } throws exception

        val result = delegate.tokenize(issuerBankId)

        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            actionInteractor(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = paymentMethodType,
                    cardNetwork = null
                )
            )
            paymentMethodModulesInteractor.getPaymentMethodDescriptors()
            asyncPaymentMethodDeeplinkInteractor(any())
        }
    }

    @Test
    fun `tokenize() should return exception when the tokenization interactor fails`() = runTest {
        val exception = Exception()
        every { actionInteractor(any()) } returns emptyFlow()
        every {
            paymentMethodModulesInteractor.getPaymentMethodDescriptors()
        } returns paymentMethodDescriptors
        every { asyncPaymentMethodDeeplinkInteractor(any()) } returns redirectUrl
        every { tokenizationInteractor.executeV2(any()) } returns flow { throw exception }

        val result = delegate.tokenize(issuerBankId)

        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            actionInteractor(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = paymentMethodType,
                    cardNetwork = null
                )
            )
            paymentMethodModulesInteractor.getPaymentMethodDescriptors()
            asyncPaymentMethodDeeplinkInteractor(any())
            primerConfig.paymentMethodIntent
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    paymentInstrumentParams = BankIssuerPaymentInstrumentParams(
                        paymentMethodType = paymentMethodType,
                        paymentMethodConfigId = paymentMethodConfigId,
                        locale = languageTag,
                        redirectionUrl = redirectUrl,
                        bankIssuer = issuerBankId
                    ),
                    paymentMethodIntent = primerSessionIntent
                )
            )
        }
    }
}
