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
import io.primer.android.UXMode
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

    private fun setupUiIfVaultMode(view: View) {
        if (checkoutConfig.uxMode == UXMode.VAULT) {
            view.findViewById<TextView>(R.id.primer_sheet_title).isVisible = false
            view.findViewById<TextView>(R.id.choose_payment_method_label).text =
                context?.getString(R.string.add_new_payment_method)
        }
    }

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

        view.findViewById<TextView>(R.id.see_all_label).setOnClickListener {
            primerViewModel.goToVaultedPaymentMethodsView()
        }

        setupUiIfVaultMode(view)

        primerViewModel.vaultedPaymentMethods.observe(viewLifecycleOwner) { paymentMethods ->

            // the views for displaying the saved payment method should not be visible
            // if the customer has not added any.
            toggleSavedPaymentMethodViewVisibility(view, paymentMethods.isEmpty())

            if (paymentMethods.isEmpty()) return@observe

            val id = primerViewModel.getSelectedPaymentMethodId()

            // select new id if can't find selected id
            val method = paymentMethods.find { it.token == id } ?: paymentMethods[0]

            primerViewModel.setSelectedPaymentMethodId(method.token)

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
                    val last4: Int = data?.last4Digits ?: 0
                    lastFourLabel.text = getString(R.string.last_four, last4)
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
}
