package io.primer.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.payments.create.CreatePaymentInteractor
import io.primer.android.domain.payments.displayMetadata.PaymentMethodsImplementationInteractor
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsDeleteInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsExchangeInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsInteractor
import io.primer.android.domain.payments.resume.ResumePaymentInteractor
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.payment.billing.BillingAddressValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("LongParameterList")
internal class PrimerViewModelFactory(
    private val configurationInteractor: ConfigurationInteractor,
    private val paymentMethodModulesInteractor: PaymentMethodModulesInteractor,
    private val paymentMethodsImplementationInteractor: PaymentMethodsImplementationInteractor,
    private val vaultedPaymentMethodsInteractor: VaultedPaymentMethodsInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val exchangeInteractor: VaultedPaymentMethodsExchangeInteractor,
    private val vaultedPaymentMethodsDeleteInteractor: VaultedPaymentMethodsDeleteInteractor,
    private val createPaymentInteractor: CreatePaymentInteractor,
    private val resumePaymentInteractor: ResumePaymentInteractor,
    private val actionInteractor: ActionInteractor,
    private val config: PrimerConfig,
    private val billingAddressValidator: BillingAddressValidator
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return PrimerViewModel(
            configurationInteractor,
            paymentMethodModulesInteractor,
            paymentMethodsImplementationInteractor,
            vaultedPaymentMethodsInteractor,
            analyticsInteractor,
            exchangeInteractor,
            vaultedPaymentMethodsDeleteInteractor,
            createPaymentInteractor,
            resumePaymentInteractor,
            actionInteractor,
            config,
            billingAddressValidator
        ) as T
    }
}
