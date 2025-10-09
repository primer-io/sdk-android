package io.primer.android.scope

import io.primer.android.core.di.DISdkComponent
import kotlinx.coroutines.flow.StateFlow

internal interface PrimerNativeUiPaymentMethodScope : DISdkComponent {

    val state: StateFlow<State>

    fun onCancel()

    data class State(
        val isProcessing: Boolean = false,
        val error: String? = null,
    )
}
