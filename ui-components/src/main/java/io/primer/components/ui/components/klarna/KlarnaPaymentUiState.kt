package io.primer.components.ui.components.klarna

import io.primer.android.klarna.api.ui.PrimerKlarnaPaymentView
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory
import io.primer.components.PrimerPaymentMethodScope

// TODO: add KDocs after stabilizing interface
sealed interface KlarnaPaymentUiState : PrimerPaymentMethodScope.PrimerPaymentMethodUiState {
    data object Loading : KlarnaPaymentUiState

    data class PaymentCategoriesLoaded(
        val paymentMethodCategories: List<KlarnaPaymentCategory>,
    ) : KlarnaPaymentUiState

    data class PaymentViewCreated(
        val paymentMethodCategories: List<KlarnaPaymentCategory>,
        val paymentView: PrimerKlarnaPaymentView,
    ) : KlarnaPaymentUiState

    data class PaymentAuthorized(
        val paymentMethodCategories: List<KlarnaPaymentCategory>,
        val paymentView: PrimerKlarnaPaymentView,
        val authorization: Authorization,
    ) : KlarnaPaymentUiState {
        data class Authorization(val isFinalized: Boolean)
    }

    data class PaymentFinalized(
        val paymentMethodCategories: List<KlarnaPaymentCategory>,
        val paymentView: PrimerKlarnaPaymentView,
        val authorization: PaymentAuthorized.Authorization,
    ) : KlarnaPaymentUiState
}
