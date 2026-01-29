package io.primer.android.scope

import io.primer.android.core.di.DISdkComponent
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import kotlinx.coroutines.flow.StateFlow

interface PrimerVaultScope : DISdkComponent {

    val state: StateFlow<State>

    fun selectForPayment(paymentMethod: PrimerVaultedPaymentMethod)
    fun selectForDeletion(paymentMethod: PrimerVaultedPaymentMethod)
    fun submit()
    fun delete()
    fun updateCvv(cvv: String)
    fun toggleEditMode()

    data class State(
        val paymentMethods: List<PrimerVaultedPaymentMethod> = emptyList(),
        val selectedPaymentMethod: PrimerVaultedPaymentMethod? = null,
        val cvv: Cvv? = null,
        val isLoading: Boolean = false,
        val isEditMode: Boolean = false,
        val error: Throwable? = null,
    )

    data class Cvv(
        val value: String = "",
        val isValid: Boolean = false,
    )
}
