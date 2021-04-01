package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import io.primer.android.R
import io.primer.android.di.DIAppComponent
import io.primer.android.logging.Logger
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.ui.SelectPaymentMethodTitle
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class SelectPaymentMethodFragment : Fragment(), DIAppComponent {

    private lateinit var viewModel: PrimerViewModel
    private lateinit var tokenizationViewModel: TokenizationViewModel

    private val checkoutConfig: CheckoutConfig by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = PrimerViewModel.getInstance(requireActivity())
        tokenizationViewModel = TokenizationViewModel.getInstance(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_select_payment_method, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container: ViewGroup = view.findViewById(R.id.primer_sheet_payment_methods_list)
        viewModel.paymentMethods.observe(viewLifecycleOwner, { paymentMethods ->
            paymentMethods.forEachIndexed { i, paymentMethod ->
                val button = paymentMethod.createButton(requireContext())
                button.layoutParams = createLayoutParams(i == 0)

                container.addView(button)

                button.setOnClickListener {
                    viewModel.setSelectedPaymentMethod(paymentMethod)
                }
            }

            container.requestLayout()
        })

        view.findViewById<SelectPaymentMethodTitle>(R.id.primer_sheet_title_layout).apply {
            setAmount(checkoutConfig.amount)
            setUXMode(checkoutConfig.uxMode)
        }
    }

    private fun createLayoutParams(isFirst: Boolean): LinearLayout.LayoutParams {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        if (!isFirst) {
            params.topMargin = 13 // FIXME why 13?
        }

        return params
    }

    companion object {

        @JvmStatic
        fun newInstance() = SelectPaymentMethodFragment()
    }
}
