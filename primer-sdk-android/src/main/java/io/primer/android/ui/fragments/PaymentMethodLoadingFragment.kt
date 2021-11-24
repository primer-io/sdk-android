package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import io.primer.android.R
import io.primer.android.di.DIAppComponent
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.viewmodel.PrimerViewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class PaymentMethodLoadingFragment : Fragment(), DIAppComponent {

    private val viewModel: PrimerViewModel by activityViewModels()

    private lateinit var selectedPaymentLogo: ImageView

    private val selectedPaymentMethodObserver = Observer<PaymentMethodDescriptor?> {
        it?.getLoadingResourceId()?.let {
            selectedPaymentLogo.setImageResource(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? =
        inflater.inflate(R.layout.fragment_payment_method_loading, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedPaymentLogo = view.findViewById(R.id.selected_payment_logo)

        viewModel.selectedPaymentMethod.observe(viewLifecycleOwner, selectedPaymentMethodObserver)
    }

    companion object {

        fun newInstance(): PaymentMethodLoadingFragment {
            return PaymentMethodLoadingFragment()
        }
    }
}
