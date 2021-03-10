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

/**
 * A simple [Fragment] subclass.
 * Use the [SelectPaymentMethodFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@KoinApiExtension
internal class SelectPaymentMethodFragment : Fragment(), DIAppComponent {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_payment_method, container, false)
    }

    private val log = Logger("checkout-fragment")
    private lateinit var viewModel: PrimerViewModel
    private lateinit var tokenizationViewModel: TokenizationViewModel
    private val checkoutConfig: CheckoutConfig by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = PrimerViewModel.getInstance(requireActivity())
        tokenizationViewModel = TokenizationViewModel.getInstance(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.paymentMethods.observe(viewLifecycleOwner, { items ->
            val container: ViewGroup = findViewById(R.id.primer_sheet_payment_methods_list)

            items.forEachIndexed { i, pm ->
                val button = pm.createButton(requireContext())
                button.layoutParams = createLayoutParams(i == 0)

                container.addView(button)

                button.setOnClickListener {
                    viewModel.setSelectedPaymentMethod(pm)
                }
            }

            container.requestLayout()
        })

        findViewById<SelectPaymentMethodTitle>(R.id.primer_sheet_title_layout).setAmount(checkoutConfig.amount)
        findViewById<SelectPaymentMethodTitle>(R.id.primer_sheet_title_layout).setUXMode(checkoutConfig.uxMode)
    }

    private fun <T : View> findViewById(id: Int): T {
        return requireView().findViewById(id)
    }

    private fun createLayoutParams(isFirst: Boolean): LinearLayout.LayoutParams {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        if (!isFirst) {
            params.topMargin = 13
        }

        return params
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SelectPaymentMethodFragment.
         */
        @JvmStatic
        fun newInstance() = SelectPaymentMethodFragment()
    }
}
