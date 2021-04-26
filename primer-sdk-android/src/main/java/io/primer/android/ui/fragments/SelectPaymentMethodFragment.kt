package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import io.primer.android.R
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.ui.SelectPaymentMethodTitle
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class SelectPaymentMethodFragment : Fragment(), DIAppComponent {

    companion object {

        @JvmStatic
        fun newInstance() = SelectPaymentMethodFragment()
    }

    private lateinit var primerViewModel: PrimerViewModel
    private lateinit var tokenizationViewModel: TokenizationViewModel

    private val checkoutConfig: CheckoutConfig by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        primerViewModel = PrimerViewModel.getInstance(requireActivity())
        tokenizationViewModel = TokenizationViewModel.getInstance(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? =
        inflater.inflate(R.layout.fragment_select_payment_method, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val container: ViewGroup = view.findViewById(R.id.primer_sheet_payment_methods_list)

        primerViewModel.paymentMethods.observe(viewLifecycleOwner) { paymentMethods ->
            paymentMethods.forEach { paymentMethod ->
                val button: View = paymentMethod.createButton(container)

                button.layoutParams = button.layoutParams.apply {
                    val layoutParams = this as LinearLayout.LayoutParams
                    layoutParams.topMargin =
                        resources.getDimensionPixelSize(R.dimen.primer_list_margin)
                }

                container.addView(button)

                button.setOnClickListener {
                    primerViewModel.selectPaymentMethod(paymentMethod)
                }
            }

            container.requestLayout()
        }

        view.findViewById<SelectPaymentMethodTitle>(R.id.primer_sheet_title_layout).apply {
            setAmount(checkoutConfig.monetaryAmount)
            setUXMode(checkoutConfig.uxMode)
        }
    }
}
