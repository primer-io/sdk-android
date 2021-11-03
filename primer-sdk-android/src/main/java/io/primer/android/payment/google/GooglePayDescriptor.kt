package io.primer.android.payment.google

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.viewmodel.PaymentMethodChecker

internal class GooglePayDescriptor constructor(
    val localConfig: PrimerConfig,
    val options: GooglePay,
    val googlePayFacade: GooglePayFacade,
    paymentMethodChecker: PaymentMethodChecker,
    config: PaymentMethodRemoteConfig,
) : PaymentMethodDescriptor(config) {

    companion object {

        const val GOOGLE_PAY_REQUEST_CODE = GooglePayFacade.GOOGLE_PAY_REQUEST_CODE
    }

    override val selectedBehaviour: SelectedPaymentMethodBehaviour =
        GooglePayBehaviour(
            paymentMethodDescriptor = this,
            googlePayPaymentMethodChecker = paymentMethodChecker,
            googlePayFacade = googlePayFacade
        )

    override val type: PaymentMethodUiType
        get() = PaymentMethodUiType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.SINGLE_USE_AND_VAULT

    val merchantId: String?
        get() = config.options?.merchantId
//            ?.replace("\"", "") // FIXME issue with kotlin serialization here

    override fun createButton(container: ViewGroup): View =
        LayoutInflater.from(container.context).inflate(
            when (options.buttonStyle) {
                GooglePay.Companion.ButtonStyle.BLACK -> R.layout.googlepay_black_button
                GooglePay.Companion.ButtonStyle.WHITE -> R.layout.googlepay_white_button_no_shadow
            },
            container,
            false
        )
}
