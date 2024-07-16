package io.primer.android.viewmodel

import com.jraska.livedata.test
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.currencyformat.interactors.FetchCurrencyFormatDataInteractor
import io.primer.android.domain.currencyformat.interactors.FormatAmountToCurrencyInteractor
import io.primer.android.domain.payments.create.CreatePaymentInteractor
import io.primer.android.domain.payments.displayMetadata.PaymentMethodsImplementationInteractor
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsDeleteInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsExchangeInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsInteractor
import io.primer.android.domain.payments.resume.ResumePaymentInteractor
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.billing.BillingAddressValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class PrimerViewModelTest {
    @MockK
    private lateinit var configurationInteractor: ConfigurationInteractor

    @MockK
    private lateinit var paymentMethodModulesInteractor: PaymentMethodModulesInteractor

    @MockK
    private lateinit var paymentMethodsImplementationInteractor: PaymentMethodsImplementationInteractor

    @MockK
    private lateinit var vaultedPaymentMethodsInteractor: VaultedPaymentMethodsInteractor

    @RelaxedMockK
    private lateinit var analyticsInteractor: AnalyticsInteractor

    @MockK
    private lateinit var exchangeInteractor: VaultedPaymentMethodsExchangeInteractor

    @MockK
    private lateinit var vaultedPaymentMethodsDeleteInteractor: VaultedPaymentMethodsDeleteInteractor

    @MockK
    private lateinit var createPaymentInteractor: CreatePaymentInteractor

    @MockK
    private lateinit var resumePaymentInteractor: ResumePaymentInteractor

    @MockK
    private lateinit var actionInteractor: ActionInteractor

    @MockK
    private lateinit var fetchCurrencyFormatDataInteractor: FetchCurrencyFormatDataInteractor

    @MockK
    private lateinit var amountToCurrencyInteractor: FormatAmountToCurrencyInteractor

    @RelaxedMockK
    private lateinit var config: PrimerConfig

    @MockK
    private lateinit var billingAddressValidator: BillingAddressValidator

    @InjectMockKs
    private lateinit var viewModel: PrimerViewModel

    @Test
    fun `setSelectedPaymentMethodId() should clear selected payment method`() {
        val paymentMethodDescriptor = mockk<PaymentMethodDescriptor>(relaxed = true)
        val observer = viewModel.selectedPaymentMethod.test()
        viewModel.selectPaymentMethod(paymentMethodDescriptor)
        viewModel.setSelectedPaymentMethodId("123")

        assertEquals(listOf(null, paymentMethodDescriptor, null), observer.valueHistory())
    }
}
