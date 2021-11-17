package io.primer.android.payment.async

import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability

internal abstract class AsyncPaymentMethodDescriptor constructor(
    open val localConfig: PrimerConfig,
    open val options: AsyncPaymentMethod,
    config: PaymentMethodRemoteConfig,
) : PaymentMethodDescriptor(config) {

    companion object {

        const val ASYNC_METHOD_REQUEST_CODE = 1002
    }

    abstract val title: String

    open val behaviours: List<SelectedPaymentMethodBehaviour> = listOf()

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = AsyncPaymentMethodBehaviour(this)

    override val type: PaymentMethodUiType
        get() = PaymentMethodUiType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.SINGLE_USE_ONLY
}
