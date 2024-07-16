package io.primer.android.data.tokenization.models.paymentInstruments.async

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.bancontactCard.AdyenBancontactSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.bankIssuer.BankIssuerSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.blik.BlikSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.dummy.PrimerDummySessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.phone.PhoneNumberSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.retailOutlets.RetailOutletsSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.webRedirect.WebRedirectSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.nolpay.NolPaySessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.stripe.ach.StripeAchSessionInfoDataRequest

internal open class BaseSessionInfoDataRequest(
    open val locale: String,
    open val platform: String = "ANDROID"
) : JSONObjectSerializable {
    companion object {

        const val PLATFORM_FIELD = "platform"
        const val LOCALE_FIELD = "locale"
        const val REDIRECTION_URL_FIELD = "redirectionUrl"

        @JvmField
        val serializer =
            JSONObjectSerializer<BaseSessionInfoDataRequest> { t ->
                when (t) {
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

                    is NolPaySessionInfoDataRequest ->
                        NolPaySessionInfoDataRequest.serializer.serialize(t)

                    is StripeAchSessionInfoDataRequest ->
                        StripeAchSessionInfoDataRequest.serializer.serialize(t)

                    else -> throw IllegalArgumentException("Missing serializer declaration for $t")
                }
            }
    }
}
