package io.primer.android.ui.fragments

import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.primer.android.PrimerSessionIntent
import io.primer.android.R
import io.primer.android.SessionState
import io.primer.android.components.ui.assets.ImageColor
import io.primer.android.components.ui.views.PrimerPaymentMethodViewFactory
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.databinding.FragmentSelectPaymentMethodBinding
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.activityViewModel
import io.primer.android.di.extension.inject
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.config.BaseDisplayMetadata
import io.primer.android.payment.utils.ButtonViewHelper.generateButtonContent
import io.primer.android.ui.CardNetwork
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.PrimerViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@Suppress("TooManyFunctions")
internal class SelectPaymentMethodFragment : Fragment(), DISdkComponent {

    companion object {

        @JvmStatic
        fun newInstance() =
            SelectPaymentMethodFragment()
    }

    private val localConfig: PrimerConfig by inject()
    private val theme: PrimerTheme by inject()
    private val methodViewFactory: PrimerPaymentMethodViewFactory by inject()

    private val primerViewModel: PrimerViewModel by
    activityViewModel<PrimerViewModel, PrimerViewModelFactory>()

    private var binding: FragmentSelectPaymentMethodBinding by autoCleaned()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectPaymentMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!primerViewModel.surchargeDisabled) {
            primerViewModel.reselectSavedPaymentMethod()
        }

        renderTitle()
        renderAmountLabel()
        renderSubtitles()
        renderSavedPaymentMethodItem()
        renderManageVaultLabel()
        renderPayButton()
        setupUiForVaultModeIfNeeded()

        addListeners()
    }

    /*
    *
    * title
    * */
    private fun renderTitle() {
        val context = requireContext()
        val textColor = theme.titleText.defaultColor.getColor(context, theme.isDarkMode)
        binding.choosePaymentMethodLabel.setTextColor(textColor)
        val fontSize = theme.titleText.fontSize.getDimension(context)
        binding.choosePaymentMethodLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
    }

    /*
    *
    * amount label
    * */
    // show title label that displays total amount minus any applies surcharges
    private fun renderAmountLabel() {
        binding.primerSheetTitleLayout.apply {
            setAmount(primerViewModel.amountLabel(localConfig))
            setUxMode(localConfig.paymentMethodIntent)
        }
    }

    /*
    *
    * subtitles
    * */
    private fun renderSubtitles() {
        val context = requireContext()
        val color = theme.subtitleText.defaultColor.getColor(context, theme.isDarkMode)
        val fontSize = theme.subtitleText.fontSize.getDimension(context)
        binding.savedPaymentMethodLabel.setTextColor(color)
        binding.otherWaysToPayLabel.setTextColor(color)
        binding.savedPaymentMethodLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
        binding.otherWaysToPayLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
    }

    /*
    *
    * saved payment method item
    * */
    private fun renderSavedPaymentMethodItem() {
        val context = requireContext()
        val contentDrawable = generateButtonContent(context, theme)
        val splash = theme.splashColor.getColor(context, theme.isDarkMode)
        val pressedStates = ColorStateList.valueOf(splash)
        val rippleDrawable = RippleDrawable(pressedStates, contentDrawable, null)
        binding.savedPaymentMethod.paymentMethodItem.background = rippleDrawable
        val textColor =
            theme.paymentMethodButton.text.defaultColor.getColor(context, theme.isDarkMode)
        binding.savedPaymentMethod.apply {
            titleLabel.setTextColor(textColor)
            titleLabel.setTextColor(textColor)
            lastFourLabel.setTextColor(textColor)
            expiryLabel.setTextColor(textColor)
        }
    }

    /*
    *
    * pay button
    * */

    private fun renderPayButton() = binding.payAllButton.apply {
        isEnabled = true
        setTheme(theme)
        setOnClickListener { onPayButtonPressed() }
    }

    private fun onPayButtonPressed() {
        binding.payAllButton.showProgress()
        lifecycleScope.launch {
            if (primerViewModel.shouldShowCaptureCvv()) {
                primerViewModel.goToVaultedPaymentCvvRecaptureView()
            } else {
                val paymentMethod = primerViewModel.selectedSavedPaymentMethod ?: return@launch
                // disable buttons and links
                disableButtons()
                primerViewModel.exchangePaymentMethodToken(paymentMethod)
            }
        }
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
                    binding.payAllButton.amount =
                        primerViewModel.amountToCurrencyString(localConfig.monetaryAmount)
                    renderVaultedItemSurchargeLabel()
                    renderAmountLabel()
                    setBusy(false)
                }

                else -> setBusy(true)
            }
        }

        // add listener with action to navigate to vault manager fragment
        binding.seeAllLabel.setOnClickListener { primerViewModel.goToVaultedPaymentMethodsView() }

        // add listener for populating payment method buttons list
        primerViewModel.paymentMethods.observe(viewLifecycleOwner) { paymentMethods ->
            addPaymentMethodsToList(
                paymentMethods,
                primerViewModel.getPaymentMethodsDisplayMetadata()
            )
        }

        // add listener for displaying selected saved payment method
        primerViewModel.vaultedPaymentMethods.observe(viewLifecycleOwner) {
            if (primerViewModel.shouldDisplaySavedPaymentMethod) {
                renderSelectedPaymentMethod()
            } else {
                binding.primerSavedPaymentSection.isVisible = false
            }
        }
    }

    private fun renderVaultedItemSurchargeLabel() {
        val text = primerViewModel.savedPaymentMethodSurchargeLabel(requireContext())
        if (primerViewModel.surchargeDisabled) {
            binding.savedPaymentMethodBox.hideSurchargeFrame()
        } else {
            binding.savedPaymentMethodBox.showSurchargeLabel(text)
        }
    }

    private fun setBusy(isBusy: Boolean) {
        binding.primerSelectPaymentMethodLayout.children.iterator()
            .forEach { it.isVisible = !isBusy }
        if (localConfig.paymentMethodIntent.isNotVault) {
            binding.primerSavedPaymentSection.isVisible = !isBusy
        } else {
            binding.primerSavedPaymentSection.isVisible = false
        }
        binding.primerSelectPaymentMethodSpinner.isGone = !isBusy
    }

    private fun renderManageVaultLabel() {
        val context = requireContext()
        binding.seeAllLabel.setTextColor(
            theme.systemText.defaultColor.getColor(
                context,
                theme.isDarkMode
            )
        )
        val fontSize = theme.systemText.fontSize.getDimension(requireContext())
        binding.seeAllLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
    }

    private fun setupUiForVaultModeIfNeeded() = when (localConfig.intent.paymentMethodIntent) {
        PrimerSessionIntent.CHECKOUT -> Unit
        PrimerSessionIntent.VAULT -> {
            binding.primerSheetTitle.isVisible = false
            binding.payAllButton.isVisible = false
            binding.choosePaymentMethodLabel.text =
                context?.getString(R.string.add_new_payment_method)
        }
    }

    private fun addPaymentMethodsToList(
        paymentMethods: List<PaymentMethodDescriptor>,
        paymentMethodsDisplayMetadata: List<BaseDisplayMetadata>
    ) {
        val factory = primerViewModel.paymentMethodButtonGroupFactory

        val boxes = factory.build(
            requireContext(),
            methodViewFactory,
            paymentMethodsDisplayMetadata,
            paymentMethods,
            onClick = { paymentMethod ->
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
            }
        )

        boxes.forEachIndexed { i, box ->
            if (primerViewModel.surchargeDisabled) {
                box.hideSurchargeFrame().run { if (i > 0) box.setMargin(0) }
            }
            binding.primerSheetPaymentMethodsList.addView(box)
        }

        binding.primerSheetPaymentMethodsList.requestLayout()
    }

    private fun disableButtons() {
        binding.primerSheetPaymentMethodsList.children.forEach { v -> v.isEnabled = false }
        binding.seeAllLabel.isEnabled = false
    }

    private fun renderSelectedPaymentMethod() {
        // ensure selected payment method is available
        val paymentMethod = primerViewModel.selectedSavedPaymentMethod ?: return

        // hide grey box frame if surcharge is disabled / not present in session
        if (primerViewModel.surchargeDisabled) binding.savedPaymentMethodBox.hideSurchargeFrame()

        primerViewModel.setSelectedPaymentMethodId(paymentMethod.token)
        binding.savedPaymentMethod.apply {
            when (paymentMethod.paymentMethodType) {
                PaymentMethodType.KLARNA.name -> {
                    val title =
                        paymentMethod.paymentInstrumentData?.sessionData?.billingAddress?.email
                    renderAlternativeSavedPaymentMethodView(title)
                    paymentMethodIcon.setImageResource(R.drawable.ic_klarna_card)
                }

                PaymentMethodType.PAYPAL.name -> {
                    val title =
                        paymentMethod.paymentInstrumentData?.externalPayerInfo?.email ?: "PayPal"
                    renderAlternativeSavedPaymentMethodView(title)
                    paymentMethodIcon.setImageResource(R.drawable.ic_paypal_card)
                }

                PaymentMethodType.PAYMENT_CARD.name -> {
                    val data = paymentMethod.paymentInstrumentData
                    titleLabel.text = data?.cardholderName
                    val last4: Int =
                        data?.last4Digits ?: throw IllegalStateException("card data is invalid!")

                    lastFourLabel.text = getString(R.string.last_four, last4)
                    val expirationYear = "${data.expirationYear}"
                    val expirationMonth = "${data.expirationMonth}".padStart(2, '0')
                    expiryLabel.text =
                        getString(R.string.expiry_date, expirationMonth, expirationYear)
                    setCardIcon(data.binData?.network)
                }

                else -> {
                    paymentMethodIcon.setImageResource(R.drawable.ic_generic_card)
                }
            }
        }
    }

    private fun renderAlternativeSavedPaymentMethodView(title: String?) {
        binding.savedPaymentMethod.apply {
            listOf(titleLabel, lastFourLabel, expiryLabel).forEach { it.isVisible = false }
            titleLabel.isVisible = true
            titleLabel.text = title
        }
    }

    private fun setCardIcon(network: String?) = binding.savedPaymentMethod.paymentMethodIcon.apply {
        val resId = CardNetwork.Type.valueOrNull(network)?.getCardBrand()?.getImageAsset(ImageColor.COLORED)
            ?: R.drawable.ic_generic_card
        setImageResource(resId)
    }
}
