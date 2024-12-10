package io.primer.android.webRedirectShared.implementation.composer.presentation.delegate

import io.primer.android.components.manager.redirect.composable.WebRedirectStep
import io.primer.android.errors.domain.models.PaymentMethodCancelledError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter

internal class WebRedirectDelegate(
    private val successHandler: CheckoutSuccessHandler,
    private val errorHandler: CheckoutErrorHandler
) {

    fun errors(): Flow<PrimerError> = errorHandler.errors
        .filter { it !is PaymentMethodCancelledError }

    fun steps(): Flow<WebRedirectStep> = successHandler.checkoutCompleted
        .combine(errorHandler.errors) { _, error ->
            if (error is PaymentMethodCancelledError) {
                WebRedirectStep.Dismissed
            } else {
                WebRedirectStep.Success
            }
        }
}
