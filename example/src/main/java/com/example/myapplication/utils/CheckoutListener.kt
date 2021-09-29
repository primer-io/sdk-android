package com.example.myapplication.utils

import io.primer.android.CheckoutEventListener
import io.primer.android.completion.ResumeHandler
import io.primer.android.events.CheckoutEvent
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.PaymentMethodToken

class CheckoutListener(
    val onTokenizeSuccess: (PaymentMethodToken, ResumeHandler) -> Unit = { _: PaymentMethodToken, _: ResumeHandler -> },
    val onTokenSelected: (PaymentMethodToken, ResumeHandler) -> Unit = { _: PaymentMethodToken, _: ResumeHandler -> },
    val onResumeSuccess: (String, ResumeHandler) -> Unit = { _: String, _: ResumeHandler -> },
    val onResumeError: (APIError) -> Unit = {},
    val onSavedPaymentInstrumentsFetched: (List<PaymentMethodToken>) -> Unit = {},
    val onApiError: (APIError) -> Unit = {},
    val onExit: () -> Unit = {},
) : CheckoutEventListener {

    override fun onCheckoutEvent(e: CheckoutEvent) {
        when (e) {
            is CheckoutEvent.TokenizationSuccess -> onTokenizeSuccess(e.data, e.resumeHandler)
            is CheckoutEvent.TokenizationError -> onApiError(e.data)
            is CheckoutEvent.ResumeSuccess -> onResumeSuccess(e.resumeToken, e.resumeHandler)
            is CheckoutEvent.ResumeError -> onResumeError(e.data)
            is CheckoutEvent.SavedPaymentInstrumentsFetched -> onSavedPaymentInstrumentsFetched(e.data)
            is CheckoutEvent.ApiError -> onApiError(e.data)
            is CheckoutEvent.Exit -> onExit()
            is CheckoutEvent.TokenSelected -> onTokenSelected(e.data, e.resumeHandler)
            else -> Unit
        }
    }
}