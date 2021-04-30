package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
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

    private lateinit var viewModel: PrimerViewModel
    private lateinit var tokenizationViewModel: TokenizationViewModel

    private val checkoutConfig: CheckoutConfig by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = PrimerViewModel.getInstance(requireActivity())
        tokenizationViewModel = TokenizationViewModel.getInstance(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? =
        inflater.inflate(R.layout.fragment_select_payment_method, container, false)

    private fun renderAlternativeSavedPaymentMethodView(view: View, title: String?) {
        listOf(R.id.title_label, R.id.last_four_label, R.id.expiry_label)
            .forEach { view.findViewById<TextView>(it).isVisible = false }
        view.findViewById<TextView>(R.id.title_label).apply {
            isVisible = true
            text = title
        }
    }

    private fun formatExpiryDate(year: Int?, month: Int?): String {
        return "$month / $year"
    }

    private fun setCardIcon(view: View, network: String?) {
        val iconView = view.findViewById<ImageView>(R.id.payment_method_icon)
        when (network) {
            "Visa" -> iconView.setImageResource(R.drawable.ic_visa_card)
            "Mastercard" -> iconView.setImageResource(R.drawable.ic_mastercard_card)
            else -> iconView.setImageResource(R.drawable.ic_generic_card)
        }
    }

    private fun toggleSavedPaymentMethodViewVisibility(view: View, listIsEmpty: Boolean) {
        val items = listOf(
            R.id.saved_payment_method_label,
            R.id.saved_payment_method,
            R.id.see_all_label,
            R.id.other_ways_to_pay_label,
        )
        items.forEach {
            view.findViewById<View>(it).isVisible = !listIsEmpty
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container: ViewGroup = view.findViewById(R.id.primer_sheet_payment_methods_list)
        viewModel.paymentMethods.observe(
            viewLifecycleOwner,
            { paymentMethods ->
                paymentMethods.forEachIndexed { i, paymentMethod ->
                    val button = paymentMethod.createButton(requireContext())
                    button.layoutParams = createLayoutParams(i == 0)

                    container.addView(button)

                    button.setOnClickListener {
                        viewModel.setSelectedPaymentMethod(paymentMethod)
                    }
                }

                container.requestLayout()
            }
        )

        viewModel.vaultedPaymentMethods.observe(viewLifecycleOwner) { paymentMethods ->

            // the views for displaying the saved payment method should not be visible
            // if the customer has not added any.
            toggleSavedPaymentMethodViewVisibility(view, paymentMethods.isEmpty())

            if (paymentMethods.isEmpty()) return@observe

            val method = paymentMethods[0]
            val titleLabel = view.findViewById<TextView>(R.id.title_label)
            val lastFourLabel = view.findViewById<TextView>(R.id.last_four_label)
            val expiryLabel = view.findViewById<TextView>(R.id.expiry_label)
            val iconView = view.findViewById<ImageView>(R.id.payment_method_icon)

            when (method.paymentInstrumentType) {
                "KLARNA_CUSTOMER_TOKEN" -> {
                    val title =
                        method.paymentInstrumentData?.sessionData?.billingAddress?.email
                    renderAlternativeSavedPaymentMethodView(view, title)
                    iconView.setImageResource(R.drawable.ic_klarna_card)
                }
                "GOCARDLESS_MANDATE" -> {
                    renderAlternativeSavedPaymentMethodView(view, "Direct Debit")
                    iconView.setImageResource(R.drawable.ic_directdebit_card)
                }
                "PAYMENT_CARD" -> {
                    val data = method.paymentInstrumentData
                    titleLabel.text = data?.cardholderName
                    if (data?.last4Digits != null) {
                        lastFourLabel.text = getString(
                            R.string.last_four,
                            data.last4Digits
                        )
                    }
                    expiryLabel.text = formatExpiryDate(
                        data?.expirationYear,
                        data?.expirationMonth
                    )
                    setCardIcon(view, data?.network)
                }
                else -> {
                    iconView.setImageResource(R.drawable.ic_generic_card)
                }
            }
        }

        view.findViewById<SelectPaymentMethodTitle>(R.id.primer_sheet_title_layout).apply {
            setAmount(checkoutConfig.monetaryAmount)
            setUXMode(checkoutConfig.uxMode)
        }

        view.findViewById<ConstraintLayout>(R.id.saved_payment_method).setOnClickListener {
            it.isSelected = !it.isSelected
            val elevation =
                if (it.isSelected) R.dimen.elevation_selected else R.dimen.elevation_unselected
            it.elevation = resources.getDimensionPixelSize(elevation).toFloat()
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
