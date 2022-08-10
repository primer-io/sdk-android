package io.primer.android.payment.paypal

import io.primer.android.PrimerSessionIntent
import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.di.DIAppComponent
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.ui.payment.LoadingState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
@ExperimentalCoroutinesApi
internal open class PayPalDescriptor constructor(
    config: PaymentMethodConfigDataResponse,
) : PaymentMethodDescriptor(config), DIAppComponent {

    private val localConfig: PrimerConfig by inject()

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = if (localConfig.paymentMethodIntent == PrimerSessionIntent.VAULT) {
            PayPalBillingAgreementBehaviour(this)
        } else {
            PayPalOrderBehaviour(this)
        }

    override val type: PaymentMethodUiType
        get() = PaymentMethodUiType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.SINGLE_USE_AND_VAULT

    override fun getLoadingState() = LoadingState(R.drawable.ic_logo_paypal_square)
}
