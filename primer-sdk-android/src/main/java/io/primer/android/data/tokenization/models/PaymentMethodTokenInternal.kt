package io.primer.android.data.tokenization.models

import io.primer.android.data.base.models.BasePaymentToken
import io.primer.android.model.dto.PaymentInstrumentData
import io.primer.android.model.dto.TokenType
import io.primer.android.threeds.data.models.ResponseCode
import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentMethodTokenInternal(
    override val token: String,
    override val paymentInstrumentType: String,
    override val paymentInstrumentData: PaymentInstrumentData? = null,
    override val vaultData: VaultData? = null,
    override val threeDSecureAuthentication: AuthenticationDetails? = null,
    val analyticsId: String,
    val tokenType: TokenType,
) : BasePaymentToken() {

    fun setClientThreeDsError(errorMessage: String) =
        this.copy(
            threeDSecureAuthentication = AuthenticationDetails(
                ResponseCode.SKIPPED,
                "CLIENT_ERROR",
                errorMessage,
                "",
                false
            )
        )
}
