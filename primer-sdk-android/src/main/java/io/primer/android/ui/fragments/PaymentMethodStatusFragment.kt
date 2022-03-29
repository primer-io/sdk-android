package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.View
import io.primer.android.presentation.payment.async.AsyncPaymentMethodViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class PaymentMethodStatusFragment : PaymentMethodLoadingFragment() {

    private val asyncPaymentMethodViewModel: AsyncPaymentMethodViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        asyncPaymentMethodViewModel.getStatus(
            requireArguments().getString(STATUS_URL_KEY).orEmpty()
        )
    }

    companion object {
        private const val STATUS_URL_KEY = "STATUS_URL"

        fun newInstance(statusUrl: String): PaymentMethodStatusFragment {
            return PaymentMethodStatusFragment().apply {
                arguments = Bundle().apply {
                    putString(STATUS_URL_KEY, statusUrl)
                }
            }
        }
    }
}
