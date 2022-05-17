package io.primer.android.viewmodel

import com.jraska.livedata.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.data.configuration.models.PaymentMethodRemoteConfig
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.deeplink.async.AsyncPaymentMethodDeeplinkInteractor
import io.primer.android.domain.payments.apaya.ApayaSessionInteractor
import io.primer.android.domain.payments.paypal.PaypalOrderInfoInteractor
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.model.Model
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.card.CARD_CVV_FIELD_NAME
import io.primer.android.payment.card.CARD_EXPIRY_FIELD_NAME
import io.primer.android.payment.card.CARD_EXPIRY_MONTH_FIELD_NAME
import io.primer.android.payment.card.CARD_EXPIRY_YEAR_FIELD_NAME
import io.primer.android.payment.card.CARD_NAME_FILED_NAME
import io.primer.android.payment.card.CARD_NUMBER_FIELD_NAME
import io.primer.android.payment.card.Card
import io.primer.android.payment.card.CreditCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.component.KoinApiExtension
import org.koin.test.KoinTest

@ExperimentalCoroutinesApi
@KoinApiExtension
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class TokenizationViewModelTest : KoinTest {

    private lateinit var viewModel: TokenizationViewModel

    @RelaxedMockK
    private lateinit var apayaSessionInteractor: ApayaSessionInteractor

    @RelaxedMockK
    private lateinit var tokenizationInteractor: TokenizationInteractor

    @RelaxedMockK
    private lateinit var paypalOrderInfoInteractor: PaypalOrderInfoInteractor

    @RelaxedMockK
    private lateinit var asyncDeeplinkInteractor: AsyncPaymentMethodDeeplinkInteractor

    @RelaxedMockK
    private lateinit var model: Model

    @RelaxedMockK
    private lateinit var config: PrimerConfig

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel =
            TokenizationViewModel(
                model,
                config,
                tokenizationInteractor,
                apayaSessionInteractor,
                paypalOrderInfoInteractor,
                asyncDeeplinkInteractor
            )
    }

    @Test
    fun `resetting the payment method emits the NONE tokenization status`() {
        val mockJson: JSONObject = mockk()
        every { mockJson.optString(CARD_NAME_FILED_NAME) } returns "name"
        every { mockJson.optString(CARD_NUMBER_FIELD_NAME) } returns "number"
        every { mockJson.optString(CARD_EXPIRY_FIELD_NAME) } returns "expiry"
        every { mockJson.optString(CARD_CVV_FIELD_NAME) } returns "cvv"
        every { mockJson.optString(CARD_EXPIRY_MONTH_FIELD_NAME) } returns "month"
        every { mockJson.optString(CARD_EXPIRY_YEAR_FIELD_NAME) } returns "year"
        val paymentMethodConfig = PaymentMethodRemoteConfig("id", PaymentMethodType.PAYMENT_CARD)
        val paymentMethodDescriptor = CreditCard(
            paymentMethodConfig,
            Card(),
            mockJson
        )
        val statusObserver = viewModel.tokenizationStatus.test()

        runTest {
            viewModel.resetPaymentMethod(paymentMethodDescriptor)
        }

        statusObserver.assertValue(TokenizationStatus.NONE)
    }

    @Test
    fun `payment method is invalid after being reset`() {

        runTest {
            viewModel.resetPaymentMethod()
        }

        assertFalse(viewModel.isValid())
    }

    @Test
    fun `tokenization relies on TokenizationInteractor instance`() {
        val mockJson: JSONObject = mockk()
        every { mockJson.optString(CARD_NAME_FILED_NAME) } returns "name"
        every { mockJson.optString(CARD_NUMBER_FIELD_NAME) } returns "number"
        every { mockJson.optString(CARD_EXPIRY_FIELD_NAME) } returns "expiry"
        every { mockJson.optString(CARD_CVV_FIELD_NAME) } returns "cvv"
        every { mockJson.optString(CARD_EXPIRY_MONTH_FIELD_NAME) } returns "month"
        every { mockJson.optString(CARD_EXPIRY_YEAR_FIELD_NAME) } returns "year"
        coEvery { tokenizationInteractor(any()) } returns flowOf("token")
        val paymentMethodConfig = PaymentMethodRemoteConfig("id", PaymentMethodType.PAYMENT_CARD)
        val paymentMethodDescriptor = CreditCard(
            paymentMethodConfig,
            Card(),
            mockJson
        )
        runTest {
            viewModel.resetPaymentMethod(paymentMethodDescriptor)

            viewModel.tokenize()
        }

        coVerify { tokenizationInteractor(any()) }
    }
}
