package io.primer.android.nolpay.implementation.validation

import io.primer.android.core.di.DISdkComponent
import io.primer.android.nolpay.api.manager.core.composable.NolPayCollectableData
import io.primer.android.paymentmethods.CollectableDataValidator

internal interface NolPayValidatorRegistry : DISdkComponent {
    fun getValidator(data: NolPayCollectableData): CollectableDataValidator<NolPayCollectableData>
}
