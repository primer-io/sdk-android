package io.primer.android.viewmodel

import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType

internal sealed interface ViewStatus {
    data object Initializing : ViewStatus

    data object SelectPaymentMethod : ViewStatus

    data object ViewVaultedPaymentMethods : ViewStatus

    data object VaultedPaymentRecaptureCvv : ViewStatus

    data class PollingStarted(
        val statusUrl: String,
        val paymentMethodType: String,
        val delay: Int = 3000,
    ) : ViewStatus

    data class ShowError(val errorType: ErrorType, val message: String?, val delay: Int = 3000) : ViewStatus

    data class ShowSuccess(
        val successType: SuccessType,
        val delay: Int = 3000,
        val checkoutAdditionalInfo: PrimerCheckoutAdditionalInfo? = null,
    ) : ViewStatus

    data object Dismiss : ViewStatus

    data object DisableDismiss : ViewStatus
}
