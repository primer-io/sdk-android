package io.primer.android.data.payments.methods.models

import io.primer.android.data.base.models.BasePaymentToken
import io.primer.android.model.dto.PaymentInstrumentData
import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentMethodVaultTokenInternal(
    private val id: String,
    override val paymentInstrumentType: String,
    override val paymentInstrumentData: PaymentInstrumentData? = null,
    override val vaultData: VaultData? = null,
    override val threeDSecureAuthentication: AuthenticationDetails? = null,
) : BasePaymentToken() {

    override val token: String = id
}
