package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import io.primer.android.di.extension.viewModel
import io.primer.android.presentation.payment.async.AsyncPaymentMethodViewModel
import io.primer.android.presentation.payment.async.AsyncPaymentMethodViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal class PaymentMethodStatusFragment : PaymentMethodLoadingFragment() {

    private val asyncPaymentMethodViewModel: AsyncPaymentMethodViewModel
        by viewModel<AsyncPaymentMethodViewModel, AsyncPaymentMethodViewModelFactory>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        asyncPaymentMethodViewModel.getStatus(
            requireArguments().getString(STATUS_URL_KEY).orEmpty(),
            requireArguments().getString(PAYMENT_METHOD_TYPE_KEY).orEmpty()
        )
    }

    companion object {
        private const val STATUS_URL_KEY = "STATUS_URL"
        private const val PAYMENT_METHOD_TYPE_KEY = "PAYMENT_METHOD_TYPE"

        fun newInstance(
            statusUrl: String,
            paymentMethodType: String
        ): PaymentMethodStatusFragment {
            return PaymentMethodStatusFragment().apply {
                arguments = bundleOf(
                    STATUS_URL_KEY to statusUrl,
                    PAYMENT_METHOD_TYPE_KEY to paymentMethodType
                )
            }
        }
    }
}
