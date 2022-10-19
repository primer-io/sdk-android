package io.primer.android.domain.payments.additionalInfo

import com.google.gson.internal.bind.util.ISO8601Utils
import io.primer.android.data.token.model.ClientToken
import java.text.DateFormat
import java.text.ParsePosition

internal interface PrimerCheckoutAdditionalInfoResolver {

    fun resolve(clientToken: ClientToken): PrimerCheckoutAdditionalInfo?
}

internal class OmiseCheckoutAdditionalInfoResolver : PrimerCheckoutAdditionalInfoResolver {

    override fun resolve(clientToken: ClientToken): PrimerCheckoutAdditionalInfo {
        return PromptPayCheckoutAdditionalInfo(
            clientToken.expiration.orEmpty(),
            clientToken.qrCodeUrl,
            clientToken.qrCode
        )
    }
}

internal class MultibancoCheckoutAdditionalInfoResolver : PrimerCheckoutAdditionalInfoResolver {

    override fun resolve(clientToken: ClientToken): PrimerCheckoutAdditionalInfo {
        return MultibancoCheckoutAdditionalInfo(
            clientToken.expiresAt.orEmpty(),
            clientToken.reference.orEmpty(),
            clientToken.entity.orEmpty()
        )
    }
}

internal class RetailOutletsCheckoutAdditionalInfoResolver : PrimerCheckoutAdditionalInfoResolver {

    private val expiresDateFormat = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.SHORT
    )

    var retailerName: String? = null

    override fun resolve(clientToken: ClientToken): PrimerCheckoutAdditionalInfo {
        return XenditCheckoutVoucherAdditionalInfo(
            clientToken.expiresAt?.let {
                ISO8601Utils.parse(it, ParsePosition(0))?.let { expiresAt ->
                    expiresDateFormat.format(expiresAt)
                }
            } ?: "",
            clientToken.reference.orEmpty(),
            retailerName,
        )
    }
}
