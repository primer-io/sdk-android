package io.primer.sample.test.nolpay

import android.nfc.Tag
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.manager.nolPay.NolPayIntent
import io.primer.android.domain.error.models.PrimerError
import io.primer.sample.test.PrimerCollectDataStepable
import io.primer.sample.test.PrimerCollectableData
import io.primer.sample.test.PrimerHeadlessCollectDataComponent
import io.primer.sample.test.PrimerHeadlessStartable
import kotlinx.coroutines.flow.Flow

sealed interface NolPayLinkCollectableData : PrimerCollectableData {

    data class NolPayPhoneData(val mobileNumber: String, val phoneCountryDiallingCode: String) :
        NolPayLinkCollectableData

    data class NolPayOtpData(val otpCode: String) : NolPayLinkCollectableData
    data class NolPayTagData(val tag: Tag) : NolPayLinkCollectableData
}

class NolPayCollectLinkDataComponent :
    PrimerHeadlessCollectDataComponent<NolPayLinkCollectableData>,
    PrimerCollectDataStepable<NolPayCollectLinkDataStep>,
    PrimerHeadlessStartable {

    override fun updateCollectedData(t: NolPayLinkCollectableData) {
    }

    override val stepFlow: Flow<NolPayCollectLinkDataStep>

    override val errorFlow: Flow<PrimerError>

    override val validationFlow: Flow<List<PrimerValidationError>>

    override fun submit() {

    }

    override fun start() {
        TODO("Not yet implemented")
    }
}