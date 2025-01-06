package io.primer.android.banks.implementation.rpc.data.models

import io.primer.android.banks.implementation.rpc.data.RpcFunction
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBank
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBankParams
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import org.json.JSONObject

internal data class IssuingBankDataRequest(
    val paymentMethodConfigId: String,
    val command: String,
    val parameters: IssuingBankDataParameters,
) : JSONObjectSerializable {
    companion object {
        internal const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        internal const val COMMAND_FIELD = "command"
        internal const val PARAMETERS_FIELD = "parameters"

        val provider =
            object : WhitelistedHttpBodyKeysProvider {
                override val values: List<WhitelistedKey> =
                    whitelistedKeys {
                        primitiveKey(PAYMENT_METHOD_CONFIG_ID_FIELD)
                        primitiveKey(COMMAND_FIELD)
                        nonPrimitiveKey(PARAMETERS_FIELD) {
                            primitiveKey(IssuingBankDataParameters.PAYMENT_METHOD_FIELD)
                            primitiveKey(IssuingBankDataParameters.LOCALE_FIELD)
                        }
                    }
            }

        @JvmField
        val serializer =
            JSONObjectSerializer<IssuingBankDataRequest> { t ->
                JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(COMMAND_FIELD, t.command)
                    put(
                        PARAMETERS_FIELD,
                        JSONSerializationUtils.getJsonObjectSerializer<IssuingBankDataParameters>()
                            .serialize(t.parameters),
                    )
                }
            }
    }
}

internal fun IssuingBankParams.toIssuingBankRequest() =
    IssuingBankDataRequest(
        paymentMethodConfigId,
        RpcFunction.FETCH_BANK_ISSUERS.name,
        IssuingBankDataParameters(
            toIssuingBankParam(paymentMethod).orEmpty(),
            locale.toLanguageTag(),
        ),
    )

internal fun IssuingBankDataResponse.toIssuingBank() = IssuingBank(id, name, disabled, iconUrl)

private fun toIssuingBankParam(paymentMethodType: String) =
    when (paymentMethodType) {
        PaymentMethodType.ADYEN_IDEAL.name -> "ideal"
        PaymentMethodType.ADYEN_DOTPAY.name -> "dotpay"
        else -> null
    }
