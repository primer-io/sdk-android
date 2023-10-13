package io.primer.android.payment.google

import io.primer.android.R
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.GooglePayButtonStyle
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.SelectedPaymentMethodManagerBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.ui.payment.LoadingState

internal class GooglePayDescriptor constructor(
    val options: GooglePay,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse
) : PaymentMethodDescriptor(config, localConfig) {

    override val selectedBehaviour: SelectedPaymentMethodBehaviour =
        SelectedPaymentMethodManagerBehaviour(options.type, localConfig.paymentMethodIntent)

    override val type: PaymentMethodUiType
        get() = PaymentMethodUiType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.SINGLE_USE_ONLY

    override fun getLoadingState() = when (options.buttonStyle) {
        GooglePayButtonStyle.BLACK ->
            LoadingState(R.drawable.ic_logo_google_pay_black_square)
        GooglePayButtonStyle.WHITE ->
            LoadingState(R.drawable.ic_logo_google_pay_square)
    }

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI))
}
