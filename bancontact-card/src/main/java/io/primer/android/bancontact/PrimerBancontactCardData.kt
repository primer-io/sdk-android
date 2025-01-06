package io.primer.android.bancontact

import io.primer.android.paymentmethods.PrimerRawData
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData

interface AydenBancontactCardCollectableData : PrimerCollectableData

data class PrimerBancontactCardData(
    val cardNumber: String,
    val expiryDate: String,
    val cardHolderName: String,
) : PrimerRawData, AydenBancontactCardCollectableData
