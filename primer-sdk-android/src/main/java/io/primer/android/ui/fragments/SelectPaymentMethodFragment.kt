package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import io.primer.android.R
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodTokenInternal
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

    private val checkoutConfig: CheckoutConfig by inject()

    private lateinit var primerViewModel: PrimerViewModel
    private lateinit var tokenizationViewModel: TokenizationViewModel

    private lateinit var motionLayout: MotionLayout
    private lateinit var titleLabel: TextView
    private lateinit var lastFourLabel: TextView
    private lateinit var expiryLabel: TextView
    private lateinit var iconView: ImageView
    private lateinit var savedPaymentMethod: ViewGroup
    private lateinit var payAllButton: Button
    private lateinit var otherWaysPayLabel: TextView
    private lateinit var paymentMethodsContainer: ViewGroup

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
        listOf(titleLabel, lastFourLabel, expiryLabel).forEach {
            it.isVisible = false
        }

        titleLabel.apply {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        motionLayout = view.findViewById(R.id.motionLayout)
        titleLabel = view.findViewById(R.id.title_label)
        lastFourLabel = view.findViewById(R.id.last_four_label)
        expiryLabel = view.findViewById(R.id.expiry_label)
        iconView = view.findViewById(R.id.payment_method_icon)
        savedPaymentMethod = view.findViewById(R.id.saved_payment_method)
        payAllButton = view.findViewById(R.id.payAllButton)
        otherWaysPayLabel = view.findViewById(R.id.other_ways_to_pay_label)
        paymentMethodsContainer = view.findViewById(R.id.primer_sheet_payment_methods_list)

        primerViewModel.paymentMethods.observe(viewLifecycleOwner) { paymentMethods ->
            paymentMethods.forEach { paymentMethod ->
                val button: View = paymentMethod.createButton(paymentMethodsContainer)

                button.layoutParams = button.layoutParams.apply {
                    val layoutParams = this as LinearLayout.LayoutParams
                    layoutParams.topMargin =
                        resources.getDimensionPixelSize(R.dimen.primer_list_margin)
                }

                paymentMethodsContainer.addView(button)

                button.setOnClickListener {
                    primerViewModel.selectPaymentMethod(paymentMethod)
                }
            }

            paymentMethodsContainer.requestLayout()
        }

        primerViewModel.vaultedPaymentMethods.observe(viewLifecycleOwner) { paymentMethods ->
            updateVaultedMethodsUi(paymentMethods)
        }

        view.findViewById<SelectPaymentMethodTitle>(R.id.primer_sheet_title_layout).apply {
            setAmount(checkoutConfig.monetaryAmount)
            setUxMode(checkoutConfig.uxMode)
        }

        view.findViewById<ConstraintLayout>(R.id.saved_payment_method).setOnClickListener {
            it.isSelected = !it.isSelected
            val elevation =
                if (it.isSelected) R.dimen.elevation_selected else R.dimen.elevation_unselected
            it.elevation = resources.getDimensionPixelSize(elevation).toFloat()

            if (it.isSelected) {
                motionLayout.transitionToState(R.id.end)

//                val height = payAllButton.height.toFloat()
//                val payAnimator = payAllButton.animate().translationYBy(height)
//                val paymentMethodsContainerAnimator =
//                    paymentMethodsContainer.animate().translationYBy(height)
//                val otherWaysPayLabelAnimator = otherWaysPayLabel.animate().translationYBy(height)
//                payAnimator.start()
//                paymentMethodsContainerAnimator.start()
//                otherWaysPayLabelAnimator.start()
            } else {
                motionLayout.transitionToState(R.id.start)

//                val height = -payAllButton.height.toFloat()
//                val payAnimator = payAllButton.animate().translationYBy(height)
//                val paymentMethodsContainerAnimator =
//                    paymentMethodsContainer.animate().translationYBy(height)
//                val otherWaysPayLabelAnimator = otherWaysPayLabel.animate().translationYBy(height)
//                payAnimator.start()
//                paymentMethodsContainerAnimator.start()
//                otherWaysPayLabelAnimator.start()
            }
        }
    }

    private fun updateVaultedMethodsUi(paymentMethods: List<PaymentMethodTokenInternal>) {
        if (paymentMethods.isEmpty()) {
            hideSavedPaymentMethodView()
        } else {
            showSavedPaymentMethodView()
        }

        if (paymentMethods.isEmpty()) return

        val method: PaymentMethodTokenInternal = paymentMethods.first()
        updateSelectedPaymentMethod(method)
    }

    private fun showSavedPaymentMethodView() {
        toggleSavedPaymentMethodViewVisibility(false)
    }

    private fun hideSavedPaymentMethodView() {
        toggleSavedPaymentMethodViewVisibility(true)
    }

    private fun toggleSavedPaymentMethodViewVisibility(listIsEmpty: Boolean) {
        val view = this.view ?: return
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

    private fun updateSelectedPaymentMethod(paymentMethod: PaymentMethodTokenInternal) {
        val view = this.view ?: return

        when (paymentMethod.paymentInstrumentType) {
            "KLARNA_CUSTOMER_TOKEN" -> {
                val title =
                    paymentMethod.paymentInstrumentData?.sessionData?.billingAddress?.email
                renderAlternativeSavedPaymentMethodView(view, title)
                iconView.setImageResource(R.drawable.ic_klarna_card)
            }
            "GOCARDLESS_MANDATE" -> {
                renderAlternativeSavedPaymentMethodView(view, "Direct Debit")
                iconView.setImageResource(R.drawable.ic_directdebit_card)
            }
            "PAYMENT_CARD" -> {
                val data = paymentMethod.paymentInstrumentData
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
}
