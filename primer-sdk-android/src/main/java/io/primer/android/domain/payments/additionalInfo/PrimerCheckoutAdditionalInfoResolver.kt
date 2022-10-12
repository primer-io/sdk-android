package io.primer.android.domain.payments.additionalInfo

import io.primer.android.data.token.model.ClientToken

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

    override fun resolve(clientToken: ClientToken): PrimerCheckoutAdditionalInfo {
        return RetailOutletsCheckoutAdditionalInfo(
            clientToken.expiresAt.orEmpty(),
            clientToken.couponCode.orEmpty(),
            clientToken.retailerName.orEmpty()
        )
    }
}
