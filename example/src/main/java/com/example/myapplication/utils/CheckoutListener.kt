package com.example.myapplication.utils

import io.primer.android.CheckoutEventListener
import io.primer.android.completion.ResumeHandler
import io.primer.android.data.action.models.ClientSessionActionsRequest
import io.primer.android.events.CheckoutEvent
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.PaymentMethodToken

class CheckoutListener(
    val onTokenizeSuccess: (PaymentMethodToken, ResumeHandler) -> Unit = { _: PaymentMethodToken, _: ResumeHandler -> },
    val onTokenSelected: (PaymentMethodToken, ResumeHandler) -> Unit = { _: PaymentMethodToken, _: ResumeHandler -> },
    val onResumeSuccess: (String, ResumeHandler) -> Unit = { _: String, _: ResumeHandler -> },
    val onResumeError: (APIError) -> Unit = {},
    val onApiError: (APIError) -> Unit = {},
    val onExit: () -> Unit = {},
    val onActions: (ClientSessionActionsRequest, (String?) -> Unit) -> Unit,
) : CheckoutEventListener {

    override fun onCheckoutEvent(e: CheckoutEvent) {
        when (e) {
            is CheckoutEvent.TokenizationSuccess -> onTokenizeSuccess(e.data, e.resumeHandler)
            is CheckoutEvent.TokenizationError -> onApiError(e.data)
            is CheckoutEvent.ResumeSuccess -> onResumeSuccess(e.resumeToken, e.resumeHandler)
            is CheckoutEvent.ResumeError -> onResumeError(e.data)
            is CheckoutEvent.ApiError -> onApiError(e.data)
            is CheckoutEvent.Exit -> onExit()
            is CheckoutEvent.TokenSelected -> onTokenSelected(e.data, e.resumeHandler)
            else -> Unit
        }
    }

    override fun onClientSessionActions(event: CheckoutEvent.OnClientSessionActions) {
        onActions(event.data) { token -> event.resumeHandler.handleClientToken(token) }
    }
}