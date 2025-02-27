package io.primer.android.klarna.implementation.session.presentation

import io.primer.android.configuration.domain.model.PaymentMethodConfig
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import org.json.JSONObject

internal class GetKlarnaAuthorizationSessionDataDelegate(
    private val configurationRepository: ConfigurationRepository,
) {
    fun getAuthorizationSessionDataOrNull(): String? {
        val extraMerchantData = getExtraMerchantDataOrNull() ?: return null

        return JSONObject().apply {
            put(
                "attachment",
                JSONObject().apply {
                    put("content_type", "application/vnd.klarna.internal.emd-v2+json")
                    put("body", extraMerchantData.toString())
                },
            )
        }.toString()
    }

    private fun getExtraMerchantDataOrNull(): JSONObject? =
        getPaymentMethods().firstOrNull { it.type == "KLARNA" }?.options?.extraMerchantData

    private fun getPaymentMethods(): List<PaymentMethodConfig> =
        configurationRepository.getConfiguration().paymentMethods
}
