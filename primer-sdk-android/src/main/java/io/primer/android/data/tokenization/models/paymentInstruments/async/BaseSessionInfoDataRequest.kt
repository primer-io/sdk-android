package io.primer.android.data.tokenization.models.paymentInstruments.async

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.bancontactCard.AdyenBancontactSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.bankIssuer.BankIssuerSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.blik.BlikSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.dummy.PrimerDummySessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.phone.PhoneNumberSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.retailOutlets.RetailOutletsSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.webRedirect.WebRedirectSessionInfoDataRequest
import org.json.JSONObject

internal open class BaseSessionInfoDataRequest(
    open val locale: String,
    open val redirectionUrl: String,
    open val platform: String = "ANDROID",
) : JSONSerializable {
    companion object {

        const val PLATFORM_FIELD = "platform"
        const val LOCALE_FIELD = "locale"
        const val REDIRECTION_URL_FIELD = "redirectionUrl"

        @JvmField
        val serializer = object : JSONSerializer<BaseSessionInfoDataRequest> {
            override fun serialize(t: BaseSessionInfoDataRequest): JSONObject {
                return when (t) {
                    is WebRedirectSessionInfoDataRequest ->
                        WebRedirectSessionInfoDataRequest.serializer.serialize(t)
                    is PhoneNumberSessionInfoDataRequest ->
                        PhoneNumberSessionInfoDataRequest.serializer.serialize(t)
                    is BlikSessionInfoDataRequest ->
                        BlikSessionInfoDataRequest.serializer.serialize(t)
                    is BankIssuerSessionInfoDataRequest ->
                        BankIssuerSessionInfoDataRequest.serializer.serialize(t)
                    is PrimerDummySessionInfoDataRequest ->
                        PrimerDummySessionInfoDataRequest.serializer.serialize(t)
                    is RetailOutletsSessionInfoDataRequest ->
                        RetailOutletsSessionInfoDataRequest.serializer.serialize(t)
                    is AdyenBancontactSessionInfoDataRequest ->
                        AdyenBancontactSessionInfoDataRequest.serializer.serialize(t)
                    else -> throw IllegalArgumentException("Missing serializer declaration for $t")
                }
            }
        }
    }
}
