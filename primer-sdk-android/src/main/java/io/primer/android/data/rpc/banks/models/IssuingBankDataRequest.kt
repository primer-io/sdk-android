package io.primer.android.data.rpc.banks.models

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.rpc.RpcFunction
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.domain.rpc.banks.models.IssuingBankParams
import org.json.JSONObject

internal data class IssuingBankDataRequest(
    val paymentMethodConfigId: String,
    val command: String,
    val parameters: IssuingBankDataParameters
) : JSONSerializable {
    companion object {

        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val COMMAND_FIELD = "command"
        private const val PARAMETERS_FIELD = "parameters"

        @JvmField
        val serializer = object : JSONSerializer<IssuingBankDataRequest> {
            override fun serialize(t: IssuingBankDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(COMMAND_FIELD, t.command)
                    put(
                        PARAMETERS_FIELD,
                        JSONSerializationUtils.getSerializer<IssuingBankDataParameters>()
                            .serialize(t.parameters)
                    )
                }
            }
        }
    }
}

internal fun IssuingBankParams.toIssuingBankRequest() = IssuingBankDataRequest(
    paymentMethodConfigId,
    RpcFunction.FETCH_BANK_ISSUERS.name,
    IssuingBankDataParameters(
        toIssuingBankParam(paymentMethod).orEmpty(),
        locale.toLanguageTag(),
    )
)

internal fun IssuingBankDataResponse.toIssuingBank() = IssuingBank(id, name, disabled, iconUrl)

private fun toIssuingBankParam(paymentMethodType: String) = when (paymentMethodType) {
    PaymentMethodType.ADYEN_IDEAL.name -> "ideal"
    PaymentMethodType.ADYEN_DOTPAY.name -> "dotpay"
    else -> null
}
