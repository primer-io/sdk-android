package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation

import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData
import io.primer.android.di.DISdkComponent

internal interface NolPayValidatorRegistry : DISdkComponent {
    fun getValidator(data: NolPayCollectableData): NolPayDataValidator<NolPayCollectableData>
}
