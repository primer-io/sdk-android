package io.primer.android.scope

import android.content.Context
import android.view.View
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

interface PrimerKlarnaScope {

    val state: StateFlow<State>

    fun selectPaymentCategory(context: Context, categoryId: String)

    fun authorizePayment()

    fun finalizePayment()

    data class Category(
        val id: String,
        val name: String,
        val url: String,
    )

    data class State(
        val step: Step,
        val categories: List<Category>,
        val selectedCategoryId: String?,
        val paymentView: WeakReference<View>?,
    )

    sealed interface Step {
        data object Loading : Step
        data object CategorySelection : Step
        data object ViewReady : Step
        data object AuthorizationStarted : Step
        data object AwaitingFinalization : Step
    }
}
