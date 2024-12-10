package io.primer.android.otp

import io.primer.android.paymentmethods.PrimerRawData
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData

interface OtpCollectableData : PrimerCollectableData

data class PrimerOtpData(val otp: String) : PrimerRawData, OtpCollectableData
