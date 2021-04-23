package io.primer.android.model.dto

import io.primer.android.model.Serialization
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.json.JSONObject

/**
 * There's an issue with JsonObject & JSONObject here - need to replace
 * them all with gson or something.
 * For now we only expose JSONObject to the public
 */

@Serializable
data class PaymentMethodTokenInternal(
    val token: String,
    val analyticsId: String,
    val tokenType: TokenType,
    val paymentInstrumentType: String,
    val paymentInstrumentData: JsonObject,
    val vaultData: VaultData?,
) {

    @Serializable
    data class VaultData(
        val customerId: String,
    )
}

internal object PaymentMethodTokenAdapter {

    fun internalToExternal(token: PaymentMethodTokenInternal): PaymentMethodToken {
        return PaymentMethodToken(
            token = token.token,
            analyticsId = token.analyticsId,
            tokenType = token.tokenType,
            paymentInstrumentType = token.paymentInstrumentType,
            paymentInstrumentData = JSONObject(token.paymentInstrumentData.toString()),
            vaultData = if (token.vaultData == null) null else PaymentMethodToken.VaultData(
                customerId = token.vaultData.customerId
            )
        )
    }

    fun externalToInternal(token: PaymentMethodToken): PaymentMethodTokenInternal {
        val json = Serialization.json
        val paymentInstrumentData =
            json.parseToJsonElement(token.paymentInstrumentData.toString()).jsonObject
        val vaultData =
            if (token.vaultData == null) null
            else PaymentMethodTokenInternal.VaultData(customerId = token.vaultData.customerId)

        return PaymentMethodTokenInternal(
            token = token.token,
            analyticsId = token.analyticsId,
            tokenType = token.tokenType,
            paymentInstrumentType = token.paymentInstrumentType,
            paymentInstrumentData = paymentInstrumentData,
            vaultData = vaultData
        )
    }
}

data class PaymentMethodToken(
    val token: String,
    val analyticsId: String,
    val tokenType: TokenType,
    val paymentInstrumentType: String,
    val paymentInstrumentData: JSONObject,
    val vaultData: VaultData?,
) {

    data class VaultData(
        val customerId: String,
    )
}
