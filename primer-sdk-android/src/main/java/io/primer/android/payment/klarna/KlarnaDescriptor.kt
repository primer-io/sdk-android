package io.primer.android.payment.klarna

import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.ui.payment.LoadingState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
@ExperimentalCoroutinesApi
internal open class KlarnaDescriptor constructor(
    val options: Klarna,
    config: PaymentMethodConfigDataResponse,
) : PaymentMethodDescriptor(config) {

    companion object {

        const val KLARNA_REQUEST_CODE = 1000
    }

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = RecurringKlarnaBehaviour(this)

    override val type: PaymentMethodUiType
        get() = PaymentMethodUiType.SIMPLE_BUTTON

    override val vaultCapability = VaultCapability.SINGLE_USE_AND_VAULT

    override fun getLoadingState() = LoadingState(R.drawable.ic_logo_klarna_square)
}
