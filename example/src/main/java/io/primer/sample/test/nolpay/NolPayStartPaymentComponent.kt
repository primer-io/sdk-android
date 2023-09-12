package io.primer.sample.test.nolpay

import android.nfc.Tag
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.domain.error.models.PrimerError
import io.primer.nolpay.models.PrimerNolPaymentCard
import io.primer.sample.test.PrimerCollectDataStepable
import io.primer.sample.test.PrimerCollectableData
import io.primer.sample.test.PrimerHeadlessCollectDataComponent
import io.primer.sample.test.PrimerHeadlessStartable
import kotlinx.coroutines.flow.Flow

sealed interface NolPayStartPaymentCollectableData : PrimerCollectableData {
    data class NolPayCardData(val nolPaymentCard: PrimerNolPaymentCard) :
        NolPayStartPaymentCollectableData

    data class NolPayTagData(val tag: Tag) : NolPayStartPaymentCollectableData
}

class NolPayStartPaymentComponent :
    PrimerHeadlessCollectDataComponent<NolPayStartPaymentCollectableData>,
    PrimerCollectDataStepable<NolPayCollectPaymentDataStep>,
    PrimerHeadlessStartable {

    override fun updateCollectedData(t: NolPayStartPaymentCollectableData) {
    }

    override val stepFlow: Flow<NolPayCollectPaymentDataStep>
        get() = TODO("Not yet implemented")
    override val errorFlow: Flow<PrimerError>
        get() = TODO("Not yet implemented")
    override val validationFlow: Flow<List<PrimerValidationError>>
        get() = TODO("Not yet implemented")

    override fun submit() {
        TODO("Not yet implemented")
    }

    override fun start() {
        TODO("Not yet implemented")
    }

}