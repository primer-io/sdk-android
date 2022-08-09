package io.primer.android.data.rpc.banks.models

import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.rpc.RpcFunction
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.domain.rpc.banks.models.IssuingBankParams
import kotlinx.serialization.Serializable

@Serializable
internal data class IssuingBankRequest(
    val paymentMethodConfigId: String,
    val command: String,
    val parameters: IssuingBankDataParameters
)

internal fun IssuingBankParams.toIssuingBankRequest() = IssuingBankRequest(
    paymentMethodConfigId,
    RpcFunction.FETCH_BANK_ISSUERS.name,
    IssuingBankDataParameters(
        toIssuingBankParam(paymentMethod).orEmpty(),
        locale.toLanguageTag(),
    )
)

internal fun IssuingBankResponse.toIssuingBank() = IssuingBank(id, name, disabled, iconUrl)

private fun toIssuingBankParam(paymentMethodType: String) = when (paymentMethodType) {
    PaymentMethodType.ADYEN_IDEAL.name -> "ideal"
    PaymentMethodType.ADYEN_DOTPAY.name -> "dotpay"
    else -> null
}
