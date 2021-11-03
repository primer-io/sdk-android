package io.primer.android.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.di.DIAppComponent
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.ui.SelectPaymentMethodTitle
import io.primer.android.ui.components.PayButton
import io.primer.android.viewmodel.PrimerViewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.content.res.ColorStateList
import io.primer.android.PaymentMethodIntent

@KoinApiExtension
internal class SelectPaymentMethodFragment : Fragment(), DIAppComponent {

    companion object {

        @JvmStatic
        fun newInstance() =
            SelectPaymentMethodFragment()
    }

    private val localConfig: PrimerConfig by inject()
    private val resumeHandlerFactory: ResumeHandlerFactory by inject()
    private val paymentMethodRepository: PaymentMethodRepository by inject()
    private val theme: PrimerTheme by inject()

    private val primerViewModel: PrimerViewModel by activityViewModels()

    private lateinit var sheetTitle: TextView
    private lateinit var titleLabel: TextView
    private lateinit var selectPaymentMethodTitle: SelectPaymentMethodTitle
    private lateinit var choosePaymentMethodLabel: TextView
    private lateinit var lastFourLabel: TextView
    private lateinit var expiryLabel: TextView
    private lateinit var iconView: ImageView
    private lateinit var savedPaymentLabel: TextView
    private lateinit var savedPaymentMethod: ViewGroup
    private lateinit var seeAllLabel: TextView
    private lateinit var payAllButton: PayButton
    private lateinit var otherWaysPayLabel: TextView
    private lateinit var otherWaysToPayStartDivider: View
    private lateinit var otherWaysToPayEndDivider: View
    private lateinit var paymentMethodsContainer: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? =
        inflater.inflate(R.layout.fragment_select_payment_method, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sheetTitle = view.findViewById(R.id.primer_sheet_title)
        titleLabel = view.findViewById(R.id.title_label)
        selectPaymentMethodTitle = view.findViewById(R.id.primer_sheet_title_layout)
        choosePaymentMethodLabel = view.findViewById(R.id.choose_payment_method_label)
        lastFourLabel = view.findViewById(R.id.last_four_label)
        expiryLabel = view.findViewById(R.id.expiry_label)
        iconView = view.findViewById(R.id.payment_method_icon)
        savedPaymentLabel = view.findViewById(R.id.saved_payment_method_label)
        savedPaymentMethod = view.findViewById(R.id.saved_payment_method)
        seeAllLabel = view.findViewById(R.id.see_all_label)
        payAllButton = view.findViewById(R.id.payAllButton)
        otherWaysPayLabel = view.findViewById(R.id.other_ways_to_pay_label)
        otherWaysToPayStartDivider = view.findViewById(R.id.otherWaysToPayStartDivider)
        otherWaysToPayEndDivider = view.findViewById(R.id.otherWaysToPayEndDivider)
        paymentMethodsContainer = view.findViewById(R.id.primer_sheet_payment_methods_list)

        context?.let { c -> render(c) }

        setupUiForVaultModeIfNeeded()

        payAllButton.setTheme(theme)
        payAllButton.amount = localConfig.monetaryAmount

        payAllButton.setOnClickListener {
            payAllButton.showProgress()
            primerViewModel.vaultedPaymentMethods.value?.find {
                it.token == primerViewModel.getSelectedPaymentMethodId()
            }?.run {

                toggleButtons(false)

                paymentMethodRepository.setPaymentMethod(this)
                EventBus.broadcast(
                    CheckoutEvent.TokenSelected(
                        PaymentMethodTokenAdapter.internalToExternal(this),
                        resumeHandlerFactory.getResumeHandler(this.paymentInstrumentType)
                    )
                )
            }
        }

        primerViewModel.paymentMethods.observe(viewLifecycleOwner) { paymentMethods ->
            addPaymentMethodsToList(paymentMethods)
        }

        primerViewModel.vaultedPaymentMethods.observe(viewLifecycleOwner) { paymentMethods ->
            updateVaultedMethodsUi(paymentMethods)
        }

        seeAllLabel.setOnClickListener {
            primerViewModel.goToVaultedPaymentMethodsView()
        }

        selectPaymentMethodTitle.apply {
            setAmount(localConfig.monetaryAmount)
            setUxMode(localConfig.paymentMethodIntent)
        }

        payAllButton.isEnabled = false

        savedPaymentMethod.setOnClickListener {
            it.isSelected = !it.isSelected

            val elevation =
                if (it.isSelected) R.dimen.elevation_selected else R.dimen.elevation_unselected
            it.elevation = resources.getDimensionPixelSize(elevation).toFloat()
            payAllButton.isEnabled = it.isSelected
        }
    }

    private fun render(context: Context) {
        renderTitle(context)
        renderSavedPaymentMethodItem(context)
        renderSubtitles(context)
        renderManageLabel(context)
    }

    private fun renderTitle(context: Context) {
        val color = theme.titleText.defaultColor.getColor(context)
        choosePaymentMethodLabel.setTextColor(color)
        val fontSize = theme.titleText.fontsize.getDimension(context)
        choosePaymentMethodLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
    }

    private val buttonStates = arrayOf(
        intArrayOf(-android.R.attr.state_selected),
        intArrayOf(android.R.attr.state_selected),
    )

    private fun generateButtonContent(context: Context): GradientDrawable {
        val contentDrawable = GradientDrawable()
        val border = theme.paymentMethodButton.border
        val unSelectedColor = border.defaultColor.getColor(context)
        val selectedColor = border.selectedColor.getColor(context)
        val colors = intArrayOf(unSelectedColor, selectedColor)
        val borderStates = ColorStateList(buttonStates, colors)
        val width = border.width.getPixels(context)
        contentDrawable.setStroke(width, borderStates)
        val background = theme.paymentMethodButton.defaultColor.getColor(context)
        contentDrawable.setColor(background)
        contentDrawable.cornerRadius = theme.paymentMethodButton.cornerRadius.getDimension(context)
        return contentDrawable
    }

    private fun renderSavedPaymentMethodItem(context: Context) {
        val contentDrawable = generateButtonContent(context)
        val splash = theme.splashColor.getColor(context)
        val pressedStates = ColorStateList.valueOf(splash)
        val rippleDrawable = RippleDrawable(pressedStates, contentDrawable, null)
        savedPaymentMethod.background = rippleDrawable
        val textColor = theme.paymentMethodButton.text.defaultColor.getColor(context)
        titleLabel.setTextColor(textColor)
        savedPaymentLabel.setTextColor(textColor)
        lastFourLabel.setTextColor(textColor)
        expiryLabel.setTextColor(textColor)
    }

    private fun renderSubtitles(context: Context) {
        val color = theme.subtitleText.defaultColor.getColor(context)
        val fontSize = theme.subtitleText.fontsize.getDimension(context)
        savedPaymentLabel.setTextColor(color)
        otherWaysPayLabel.setTextColor(color)
        savedPaymentLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
        otherWaysPayLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
    }

    private fun renderManageLabel(context: Context) {
        seeAllLabel.setTextColor(theme.systemText.defaultColor.getColor(context))
        seeAllLabel.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            theme.systemText.fontsize.getDimension(requireContext()),
        )
    }

    private fun setupUiForVaultModeIfNeeded() {
        when (localConfig.intent.paymentMethodIntent) {
            PaymentMethodIntent.CHECKOUT -> return
            PaymentMethodIntent.VAULT -> {
                sheetTitle.isVisible = false
                payAllButton.isVisible = false
                choosePaymentMethodLabel.text = context?.getString(R.string.add_new_payment_method)
            }
        }
    }

    private fun toggleButtons(enabled: Boolean) {
        paymentMethodsContainer.children.forEach { v ->
            v.isEnabled = enabled
        }

        seeAllLabel.isEnabled = enabled
    }

    private fun addPaymentMethodsToList(paymentMethods: List<PaymentMethodDescriptor>) {
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
                toggleButtons(false)
            }
        }

        paymentMethodsContainer.requestLayout()
    }

    private fun updateVaultedMethodsUi(paymentMethods: List<PaymentMethodTokenInternal>) {
        val shouldShowSavedPaymentMethod =
            paymentMethods.isNotEmpty() && localConfig.paymentMethodIntent.isNotVault

        if (shouldShowSavedPaymentMethod) {
            showSavedPaymentMethodView()

            val id = primerViewModel.getSelectedPaymentMethodId()
            val method = paymentMethods.find { it.token == id } ?: paymentMethods.first()
            updateSelectedPaymentMethod(method)
        } else {
            hideSavedPaymentMethodView()
        }
    }

    private fun showSavedPaymentMethodView() {
        toggleSavedPaymentMethodViewVisibility(true)
    }

    private fun hideSavedPaymentMethodView() {
        toggleSavedPaymentMethodViewVisibility(false)
    }

    private fun toggleSavedPaymentMethodViewVisibility(show: Boolean) {
        listOf(
            savedPaymentLabel,
            savedPaymentMethod,
            seeAllLabel,
            payAllButton,
            otherWaysToPayStartDivider,
            otherWaysToPayEndDivider,
        ).forEach {
            it.isVisible = show
        }
    }

    private fun updateSelectedPaymentMethod(paymentMethod: PaymentMethodTokenInternal) {
        primerViewModel.setSelectedPaymentMethodId(paymentMethod.token)

        when (paymentMethod.paymentInstrumentType) {
            "KLARNA_CUSTOMER_TOKEN" -> {
                val title =
                    paymentMethod.paymentInstrumentData?.sessionData?.billingAddress?.email
                renderAlternativeSavedPaymentMethodView(title)
                iconView.setImageResource(R.drawable.ic_klarna_card)
            }
            "PAYPAL_BILLING_AGREEMENT" -> {
                val title =
                    paymentMethod.paymentInstrumentData?.externalPayerInfo?.email ?: "PayPal"
                renderAlternativeSavedPaymentMethodView(title)
                iconView.setImageResource(R.drawable.ic_paypal_card)
            }
            "GOCARDLESS_MANDATE" -> {
                renderAlternativeSavedPaymentMethodView("Direct Debit")
                iconView.setImageResource(R.drawable.ic_directdebit_card)
            }
            "PAYMENT_CARD" -> {
                val data = paymentMethod.paymentInstrumentData
                titleLabel.text = data?.cardholderName
                val last4: Int = data?.last4Digits ?: throw Error("card data is invalid!")

                lastFourLabel.text = getString(R.string.last_four, last4)
                // FIXME should use string resources (and pulled out to be testable)
                expiryLabel.text = "${data.expirationYear} / ${data.expirationMonth}"
                setCardIcon(data.network)
            }
            else -> {
                iconView.setImageResource(R.drawable.ic_generic_card)
            }
        }
    }

    private fun renderAlternativeSavedPaymentMethodView(title: String?) {
        listOf(titleLabel, lastFourLabel, expiryLabel).forEach {
            it.isVisible = false
        }

        titleLabel.apply {
            isVisible = true
            text = title
        }
    }

    private fun setCardIcon(network: String?) {
        when (network) {
            "Visa" -> iconView.setImageResource(R.drawable.ic_visa_card)
            "Mastercard" -> iconView.setImageResource(R.drawable.ic_mastercard_card)
            else -> iconView.setImageResource(R.drawable.ic_generic_card)
        }
    }
}
