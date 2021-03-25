package io.primer.android.viewmodel

import android.graphics.Color
import com.jraska.livedata.test
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.PaymentMethod
import io.primer.android.UniversalCheckoutTheme
import io.primer.android.di.DIAppContext
import io.primer.android.model.Model
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.card.CARD_CVV_FIELD_NAME
import io.primer.android.payment.card.CARD_EXPIRY_FIELD_NAME
import io.primer.android.payment.card.CARD_EXPIRY_MONTH_FIELD_NAME
import io.primer.android.payment.card.CARD_EXPIRY_YEAR_FIELD_NAME
import io.primer.android.payment.card.CARD_NAME_FILED_NAME
import io.primer.android.payment.card.CARD_NUMBER_FIELD_NAME
import io.primer.android.payment.card.CreditCard
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class TokenizationViewModelTest : KoinTest {

    private lateinit var viewModel: TokenizationViewModel

    @RelaxedMockK
    private lateinit var primerViewModel: PrimerViewModel

    @RelaxedMockK
    private lateinit var model: Model

    // FIXME we're forced to provide a theme in tests because of the static calls that happen if you don't
    private val theme = UniversalCheckoutTheme(
        0f,
        0f,
        Color.BLACK,
        Color.BLACK,
        Color.BLACK,
        Color.BLACK,
        Color.BLACK,
        Color.BLACK,
        Color.BLACK,
        Color.BLACK,
        Color.BLACK,
        Color.BLACK,
        Color.BLACK,
        UniversalCheckoutTheme.WindowMode.FULL_HEIGHT
    )

    private val config = CheckoutConfig(clientToken = "t0k3n", theme = theme)

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        val testApp: KoinApplication = startKoin {
            modules(
                module {
                    single { model }
                    single { config }
                })
        }
        DIAppContext.app = testApp // FIXME we have to hack this this way because of DIAppComponent and DIAppContext

        viewModel = TokenizationViewModel()
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
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
        val paymentMethodConfig = PaymentMethodRemoteConfig("id", "type")
        val paymentMethodDescriptor = CreditCard(primerViewModel, paymentMethodConfig, PaymentMethod.Card(), mockJson)
        val statusObserver = viewModel.status.test()

        viewModel.reset(paymentMethodDescriptor)

        statusObserver.assertValue(TokenizationStatus.NONE)
    }

    @Test
    fun `payment method is invalid after being reset`() {

        viewModel.reset()

        assertFalse(viewModel.isValid())
    }

    @Test
    fun `tokenization relies on Model instance`() {
        val mockJson: JSONObject = mockk()
        every { mockJson.optString(CARD_NAME_FILED_NAME) } returns "name"
        every { mockJson.optString(CARD_NUMBER_FIELD_NAME) } returns "number"
        every { mockJson.optString(CARD_EXPIRY_FIELD_NAME) } returns "expiry"
        every { mockJson.optString(CARD_CVV_FIELD_NAME) } returns "cvv"
        every { mockJson.optString(CARD_EXPIRY_MONTH_FIELD_NAME) } returns "month"
        every { mockJson.optString(CARD_EXPIRY_YEAR_FIELD_NAME) } returns "year"
        val paymentMethodConfig = PaymentMethodRemoteConfig("id", "type")
        val paymentMethodDescriptor = CreditCard(primerViewModel, paymentMethodConfig, PaymentMethod.Card(), mockJson)
        viewModel.reset(paymentMethodDescriptor)

        viewModel.tokenize()

        verify { model.tokenize(paymentMethodDescriptor) }
    }
}
