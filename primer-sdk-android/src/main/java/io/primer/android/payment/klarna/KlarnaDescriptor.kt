package io.primer.android.payment.klarna

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class KlarnaDescriptor constructor(
    val localConfig: PrimerConfig,
    val options: Klarna,
    config: PaymentMethodRemoteConfig,
) : PaymentMethodDescriptor(config) {

    companion object {

        const val KLARNA_REQUEST_CODE = 1000
    }

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = RecurringKlarnaBehaviour(this)

    override val type: PaymentMethodUiType
        get() = PaymentMethodUiType.SIMPLE_BUTTON

    override val vaultCapability = VaultCapability.SINGLE_USE_AND_VAULT

    override fun createButton(container: ViewGroup): View =
        LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_klarna,
            container,
            false
        )

    override fun getLoadingResourceId() = R.drawable.ic_logo_klarna_square
}
