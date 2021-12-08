package io.primer.android.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import android.widget.ProgressBar
import androidx.core.view.isGone
import io.primer.android.PaymentMethodIntent
import io.primer.android.SessionState
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.ui.components.PaymentMethodButtonGroupBox

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
    private lateinit var paymentMethodsContainer: ViewGroup
    private lateinit var spinner: ProgressBar
    private lateinit var savedPaymentMethodBox: PaymentMethodButtonGroupBox
    private lateinit var savedPaymentMethodSection: ViewGroup
    private lateinit var layout: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? =
        inflater.inflate(R.layout.fragment_select_payment_method, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!primerViewModel.surchargeDisabled) {
            primerViewModel.reselectSavedPaymentMethod()
        }
        bindViewComponents()
        renderTitle()
        renderAmountLabel()
        renderSubtitles()
        renderSavedPaymentMethodItem()
        renderManageVaultLabel()
        renderPayButton()
        setupUiForVaultModeIfNeeded()

        addListeners()

        selectPaymentMethodTitle.apply {
            setAmount(localConfig.monetaryAmount)
            setUxMode(localConfig.paymentMethodIntent)
        }
    }

    /*
    *
    * bind view components
    * */
    private fun bindViewComponents() {
        val view = view ?: return
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
        paymentMethodsContainer = view.findViewById(R.id.primer_sheet_payment_methods_list)
        spinner = view.findViewById(R.id.primer_select_payment_method_spinner)
        savedPaymentMethodBox = view.findViewById(R.id.saved_payment_method_box)
        savedPaymentMethodSection = view.findViewById(R.id.primer_saved_payment_section)
        layout = view.findViewById<ViewGroup>(R.id.primer_select_payment_method_layout)
    }

    /*
    *
    * title
    * */
    private fun renderTitle() {
        val context = requireContext()
        val textColor = theme.titleText.defaultColor.getColor(context, theme.isDarkMode)
        choosePaymentMethodLabel.setTextColor(textColor)
        val fontSize = theme.titleText.fontsize.getDimension(context)
        choosePaymentMethodLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
    }

    /*
    *
    * amount label
    * */
    // show title label that displays total amount minus any applies surcharges
    private fun renderAmountLabel() {
        selectPaymentMethodTitle.setAmount(primerViewModel.amountLabelMonetaryAmount(localConfig))
        selectPaymentMethodTitle.setUxMode(localConfig.paymentMethodIntent)
    }

    /*
    *
    * subtitles
    * */
    private fun renderSubtitles() {
        val context = requireContext()
        val color = theme.subtitleText.defaultColor.getColor(context, theme.isDarkMode)
        val fontSize = theme.subtitleText.fontsize.getDimension(context)
        savedPaymentLabel.setTextColor(color)
        otherWaysPayLabel.setTextColor(color)
        savedPaymentLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
        otherWaysPayLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
    }

    /*
    *
    * saved payment method item
    * */
    private fun renderSavedPaymentMethodItem() {
        val context = requireContext()
        val contentDrawable = generateButtonContent(context)
        val splash = theme.splashColor.getColor(context, theme.isDarkMode)
        val pressedStates = ColorStateList.valueOf(splash)
        val rippleDrawable = RippleDrawable(pressedStates, contentDrawable, null)
        savedPaymentMethod.background = rippleDrawable
        val textColor =
            theme.paymentMethodButton.text.defaultColor.getColor(context, theme.isDarkMode)
        titleLabel.setTextColor(textColor)
        savedPaymentLabel.setTextColor(textColor)
        lastFourLabel.setTextColor(textColor)
        expiryLabel.setTextColor(textColor)
    }

    /*
    *
    * pay button
    * */

    private fun renderPayButton() {
        payAllButton.isEnabled = true
        payAllButton.setTheme(theme)
        payAllButton.setOnClickListener { onPayButtonPressed() }
    }

    private fun onPayButtonPressed() {
        payAllButton.showProgress()
        val paymentMethod = primerViewModel.selectedSavedPaymentMethod ?: return

        // disable buttons and links
        disableButtons()

        // update payment method
        paymentMethodRepository.setPaymentMethod(paymentMethod)

        // get token
        val paymentMethodToken = PaymentMethodTokenAdapter.internalToExternal(paymentMethod)

        // emit event
        val handler = resumeHandlerFactory.getResumeHandler(paymentMethod.paymentInstrumentType)
        EventBus.broadcast(CheckoutEvent.TokenSelected(paymentMethodToken, handler))
    }

    /*
    *
    * add listeners
    * */

    private fun addListeners() {

        // add listener for re-rendering based on session state
        primerViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                SessionState.AWAITING_USER -> {
                    payAllButton.amount = localConfig.monetaryAmount
                    renderVaultedItemSurchargeLabel()
                    renderAmountLabel()
                    setBusy(false)
                }
                else -> setBusy(true)
            }
        }

        // add listener with action to navigate to vault manager fragment
        seeAllLabel.setOnClickListener { primerViewModel.goToVaultedPaymentMethodsView() }

        // add listener for populating payment method buttons list
        primerViewModel.paymentMethods.observe(viewLifecycleOwner) { paymentMethods ->
            addPaymentMethodsToList(paymentMethods)
        }

        // add listener for displaying selected saved payment method
        primerViewModel.vaultedPaymentMethods.observe(viewLifecycleOwner) { paymentMethods ->
            if (primerViewModel.shouldDisplaySavedPaymentMethod) renderSelectedPaymentMethod()
            else savedPaymentMethodSection.isVisible = false
        }
    }

    private fun renderVaultedItemSurchargeLabel() {
        val text = primerViewModel.savedPaymentMethodSurchargeLabel(requireContext())
        if (primerViewModel.surchargeDisabled) savedPaymentMethodBox.hideSurchargeFrame()
        else savedPaymentMethodBox.showSurchargeLabel(text)
    }

    private fun setBusy(isBusy: Boolean) {
        layout.children.iterator().forEach { it.isVisible = !isBusy }
        if (localConfig.paymentMethodIntent.isNotVault) {
            savedPaymentMethodSection.isVisible = !isBusy
        } else {
            savedPaymentMethodSection.isVisible = false
        }
        spinner.isGone = !isBusy
    }

    private val buttonStates = arrayOf(
        intArrayOf(-android.R.attr.state_selected),
        intArrayOf(android.R.attr.state_selected),
    )

    private fun generateButtonContent(context: Context): GradientDrawable {
        val contentDrawable = GradientDrawable()
        val border = theme.paymentMethodButton.border
        val unSelectedColor = border.defaultColor.getColor(context, theme.isDarkMode)
        val selectedColor = border.selectedColor.getColor(context, theme.isDarkMode)
        val colors = intArrayOf(unSelectedColor, selectedColor)
        val borderStates = ColorStateList(buttonStates, colors)
        val width = border.width.getPixels(context)
        contentDrawable.setStroke(width, borderStates)
        val background = theme.paymentMethodButton.defaultColor.getColor(context, theme.isDarkMode)
        contentDrawable.setColor(background)
        contentDrawable.cornerRadius = theme.paymentMethodButton.cornerRadius.getDimension(context)
        return contentDrawable
    }

    private fun renderManageVaultLabel() {
        val context = requireContext()
        seeAllLabel.setTextColor(theme.systemText.defaultColor.getColor(context, theme.isDarkMode))
        val fontSize = theme.systemText.fontsize.getDimension(requireContext())
        seeAllLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
    }

    private fun setupUiForVaultModeIfNeeded() = when (localConfig.intent.paymentMethodIntent) {
        PaymentMethodIntent.CHECKOUT -> Unit
        PaymentMethodIntent.VAULT -> {
            sheetTitle.isVisible = false
            payAllButton.isVisible = false
            choosePaymentMethodLabel.text = context?.getString(R.string.add_new_payment_method)
        }
    }

    private fun addPaymentMethodsToList(paymentMethods: List<PaymentMethodDescriptor>) {
        val factory = primerViewModel.paymentMethodButtonGroupFactory

        val boxes = factory.build(requireContext(), paymentMethods, onClick = { paymentMethod ->
            // ensure other buttons can't be clicked
            disableButtons()

            // select payment method
            primerViewModel.selectPaymentMethod(paymentMethod)

            // handle non-form cases
            paymentMethod.behaviours.firstOrNull()?.let {
                val isNotForm = paymentMethod.type != PaymentMethodUiType.FORM
                if (isNotForm) {
                    primerViewModel.executeBehaviour(it)
                }
            }
        })

        boxes.forEachIndexed { i, box ->
            if (primerViewModel.surchargeDisabled) {
                box.hideSurchargeFrame().run { if (i > 0) box.setTopMargin(0) }
            }
            paymentMethodsContainer.addView(box)
        }

        paymentMethodsContainer.requestLayout()
    }

    private fun disableButtons() {
        paymentMethodsContainer.children.forEach { v -> v.isEnabled = false }
        seeAllLabel.isEnabled = false
    }

    private fun renderSelectedPaymentMethod() {
        // ensure selected payment method is available
        val paymentMethod = primerViewModel.selectedSavedPaymentMethod ?: return

        // hide grey box frame if surcharge is disabled / not present in session
        if (primerViewModel.surchargeDisabled) savedPaymentMethodBox.hideSurchargeFrame()

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
                val expirationYear = "${data.expirationYear}"
                val expirationMonth = "${data.expirationMonth}".padStart(2, '0')
                expiryLabel.text = getString(R.string.expiry_date, expirationMonth, expirationYear)
                setCardIcon(data.network)
            }
            else -> {
                iconView.setImageResource(R.drawable.ic_generic_card)
            }
        }
    }

    private fun renderAlternativeSavedPaymentMethodView(title: String?) {
        listOf(titleLabel, lastFourLabel, expiryLabel).forEach { it.isVisible = false }
        titleLabel.isVisible = true
        titleLabel.text = title
    }

    private fun setCardIcon(network: String?) {
        when (network) {
            "Visa" -> iconView.setImageResource(R.drawable.ic_visa_card)
            "Mastercard" -> iconView.setImageResource(R.drawable.ic_mastercard_card)
            else -> iconView.setImageResource(R.drawable.ic_generic_card)
        }
    }
}
