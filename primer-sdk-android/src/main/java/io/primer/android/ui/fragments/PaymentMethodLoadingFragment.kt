package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.databinding.FragmentPaymentMethodLoadingBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.viewmodel.PrimerViewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class PaymentMethodLoadingFragment : Fragment(), DIAppComponent {

    private val viewModel: PrimerViewModel by activityViewModels()

    private var binding: FragmentPaymentMethodLoadingBinding by autoCleaned()

    private val selectedPaymentMethodObserver = Observer<PaymentMethodDescriptor?> { descriptor ->
        descriptor?.getLoadingState()?.let {
            logAnalytics(descriptor.config.type)
            binding.apply {
                selectedPaymentLogo.setImageResource(it.imageResIs)
                it.textResId?.let {
                    selectedPaymentLoadingText.isVisible = true
                    progressBar.isVisible = false
                    selectedPaymentLoadingText.text = getString(it)
                } ?: run {
                    selectedPaymentLoadingText.isVisible = false
                    progressBar.isVisible = true
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPaymentMethodLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectedPaymentMethod.observe(viewLifecycleOwner, selectedPaymentMethodObserver)
    }

    private fun logAnalytics(type: PaymentMethodType) =
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.VIEW,
                ObjectType.LOADER,
                Place.PAYMENT_METHOD_LOADING,
                null,
                PaymentMethodContextParams(type)
            )
        )

    companion object {

        fun newInstance(): PaymentMethodLoadingFragment {
            return PaymentMethodLoadingFragment()
        }
    }
}
