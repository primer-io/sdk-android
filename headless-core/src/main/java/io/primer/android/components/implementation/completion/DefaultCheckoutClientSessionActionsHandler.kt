package io.primer.android.components.implementation.completion

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.clientSessionActions.domain.handlers.CheckoutClientSessionActionsHandler
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.launch

internal class DefaultCheckoutClientSessionActionsHandler(
    private val analyticsRepository: AnalyticsRepository,
    private val checkoutErrorHandler: CheckoutErrorHandler,
    private val coroutineDispatcher: MainCoroutineDispatcher = Dispatchers.Main,
) : CheckoutClientSessionActionsHandler {
    override fun onClientSessionUpdateStarted() {
        analyticsRepository.addEvent(
            SdkFunctionParams(
                HeadlessUniversalCheckoutAnalyticsConstants.ON_BEFORE_CLIENT_SESSION_UPDATED,
            ),
        )
        coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
            PrimerHeadlessUniversalCheckout.instance.checkoutListener?.onBeforeClientSessionUpdated()
        }
    }

    override fun onClientSessionUpdateSuccess(clientSession: PrimerClientSession) {
        analyticsRepository.addEvent(
            SdkFunctionParams(
                HeadlessUniversalCheckoutAnalyticsConstants.ON_CLIENT_SESSION_UPDATED,
            ),
        )
        coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
            PrimerHeadlessUniversalCheckout.instance.checkoutListener?.onClientSessionUpdated(
                clientSession = clientSession,
            )
        }
    }

    override fun onClientSessionUpdateError(error: PrimerError) {
        coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
            CoroutineScope(Dispatchers.Main).launch {
                checkoutErrorHandler.handle(error = error, payment = null)
            }
        }
    }
}
