package io.primer.android.phoneNumber

import io.primer.android.paymentmethods.PrimerRawData
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData

interface PhoneNumberCollectableData : PrimerCollectableData

data class PrimerPhoneNumberData(val phoneNumber: String) : PrimerRawData, PhoneNumberCollectableData
