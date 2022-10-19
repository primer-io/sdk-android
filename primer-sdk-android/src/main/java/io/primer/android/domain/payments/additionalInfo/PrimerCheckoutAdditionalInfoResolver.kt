package io.primer.android.domain.payments.additionalInfo

import io.primer.android.data.token.model.ClientToken
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

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

    private val dateFormatISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val expiresDateFormat = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.SHORT
    )

    var retailerName: String? = null

    override fun resolve(clientToken: ClientToken): PrimerCheckoutAdditionalInfo {
        return XenditCheckoutVoucherAdditionalInfo(
            clientToken.expiresAt?.let {
                dateFormatISO.parse(it)?.let { expiresAt ->
                    expiresDateFormat.format(expiresAt)
                }
            } ?: "",
            clientToken.reference.orEmpty(),
            retailerName,
        )
    }
}
