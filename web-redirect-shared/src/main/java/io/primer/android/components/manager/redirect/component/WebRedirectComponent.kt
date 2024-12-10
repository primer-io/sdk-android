package io.primer.android.components.manager.redirect.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.webRedirectShared.implementation.composer.presentation.delegate.WebRedirectDelegate
import io.primer.android.webRedirectShared.implementation.composer.presentation.delegate.WebRedirectLoggingDelegate
import io.primer.android.components.manager.formWithRedirect.component.PrimerHeadlessRedirectComponent
import io.primer.android.components.manager.formWithRedirect.composable.RedirectCollectableData
import io.primer.android.components.manager.redirect.composable.WebRedirectStep
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

internal class WebRedirectComponent(
    private val paymentMethodType: String,
    webRedirectDelegate: WebRedirectDelegate,
    private val loggingDelegate: WebRedirectLoggingDelegate
) : ViewModel(),
    PrimerHeadlessRedirectComponent<RedirectCollectableData, WebRedirectStep> {
    override val componentError: Flow<PrimerError> = webRedirectDelegate.errors()

    override val componentValidationStatus: Flow<PrimerValidationStatus<RedirectCollectableData>> =
        emptyFlow()

    override val componentStep: Flow<WebRedirectStep> =
        webRedirectDelegate.steps().onStart { emit(WebRedirectStep.Loading) }

    override fun start() {
        componentError.onEach {
            loggingDelegate.logError(error = it, paymentMethodType = paymentMethodType)
        }.launchIn(viewModelScope)
        componentStep.onEach {
            loggingDelegate.logStep(webRedirectStep = it, paymentMethodType = paymentMethodType)
        }.launchIn(viewModelScope)
    }
}
