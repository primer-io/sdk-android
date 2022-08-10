package io.primer.android.payment.google

import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.ui.payment.LoadingState
import io.primer.android.viewmodel.PaymentMethodChecker

internal class GooglePayDescriptor constructor(
    val localConfig: PrimerConfig,
    val options: GooglePay,
    val googlePayFacade: GooglePayFacade,
    paymentMethodChecker: PaymentMethodChecker,
    config: PaymentMethodConfigDataResponse,
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

    override fun getLoadingState() = when (options.buttonStyle) {
        GooglePayButtonStyle.BLACK ->
            LoadingState(R.drawable.ic_logo_google_pay_black_square)
        GooglePayButtonStyle.WHITE ->
            LoadingState(R.drawable.ic_logo_google_pay_square)
    }
}
