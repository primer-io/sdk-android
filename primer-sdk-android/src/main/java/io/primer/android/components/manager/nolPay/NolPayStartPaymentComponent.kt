package io.primer.android.components.manager.nolPay

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.components.manager.core.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessStepable
import kotlinx.coroutines.flow.Flow


class NolPayStartPaymentComponent :
    PrimerHeadlessCollectDataComponent<NolPayStartPaymentCollectableData>,
    PrimerHeadlessStepable<NolPayCollectPaymentDataStep>,
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