package io.primer.android.ui.fragments

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.R
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.components.assets.ui.getCardImageAsset
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.databinding.PrimerFragmentVaultedPaymentMethodCvvRecaptureBinding
import io.primer.android.displayMetadata.domain.model.ImageColor
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.utils.hideKeyboard
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
internal class VaultedPaymentMethodsCvvRecaptureFragment : BaseFragment() {
    private var binding: PrimerFragmentVaultedPaymentMethodCvvRecaptureBinding by autoCleaned()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            PrimerFragmentVaultedPaymentMethodCvvRecaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setBackButtonOnClick()
        setupCvvInputView()
        setupPaymentMethodSubmitButton()
        setCardDetails()
        setTheme()
        focusCvvInput()

        logAnalyticsPresented()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logAnalyticsDismiss()
    }

    private fun setBackButtonOnClick() {
        getToolbar()?.getBackButton()?.setOnClickListener { view ->
            view.hideKeyboard()
            logAnalyticsBackPressed()
            primerViewModel.goToSelectPaymentMethodsView()
        }
    }

    private fun setupCvvInputView() {
        binding.vaultedPaymentMethodCvvCardCvvInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (binding.vaultedPaymentMethodCvvBtnSubmit.isEnabled) {
                    binding.vaultedPaymentMethodCvvBtnSubmit.performClick()
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun setupPaymentMethodSubmitButton() {
        binding.vaultedPaymentMethodCvvBtnSubmit.apply {
            isEnabled = false
            text = getString(R.string.continue_button)
            setOnClickListener { view ->
                view.hideKeyboard()
                logAnalyticsSubmitPressed()
                toggleLoading(true)

                val paymentMethod = primerViewModel.selectedSavedPaymentMethod ?: return@setOnClickListener
                val cvv = binding.vaultedPaymentMethodCvvCardCvvInput.text.toString().trim()
                primerViewModel.exchangePaymentMethodToken(paymentMethod, PrimerVaultedCardAdditionalData(cvv = cvv))
            }
        }
    }

    private fun setCardDetails() {
        val paymentMethod = primerViewModel.selectedSavedPaymentMethod ?: return
        val data = paymentMethod.paymentInstrumentData
        val last4: Int = requireNotNull(data.last4Digits) { "card data is invalid!" }
        binding.vaultedPaymentMethodCvvLastFourLabel.text = getString(R.string.last_four, last4)
        binding.vaultedPaymentMethodCvvDescLabel.text =
            getString(
                R.string.primer_cvv_recapture_explanation,
                data.binData?.network?.let { CardNetwork.lookupByCardNetwork(it).cvvLength },
            )
        setCardIcon(data.binData?.network)
    }

    private fun setCardIcon(network: String?) =
        binding.vaultedPaymentMethodCvvCardIcon.apply {
            val resId =
                CardNetwork.Type.valueOrNull(network)?.getCardImageAsset(ImageColor.COLORED)
                    ?: R.drawable.ic_generic_card
            setImageResource(resId)
        }

    private fun toggleLoading(on: Boolean) {
        binding.vaultedPaymentMethodCvvBtnSubmit.setProgress(on)
        binding.vaultedPaymentMethodCvvBtnSubmit.isEnabled = on.not()
        binding.vaultedPaymentMethodCvvCardCvvInput.apply {
            isEnabled = on.not()
            alpha = if (on) ALPHA_HALF else ALPHA_VISIBLE
        }
    }

    private fun setTheme() {
        setTitleTheme()
        setCvvInputTheme()
    }

    private fun setTitleTheme() {
        getToolbar()?.showOnlyTitle(R.string.primer_cvv_recapture_title)
    }

    private fun setCvvInputTheme() {
        val context = requireContext()
        val cvvView = binding.vaultedPaymentMethodCvvCardCvvLabel
        val fontSize = theme.input.text.fontSize.getDimension(context)
        val color = theme.input.text.defaultColor.getColor(context, theme.isDarkMode)
        cvvView.editText?.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
        cvvView.editText?.setTextColor(color)
        cvvView.setupEditTextTheme()
        cvvView.setupEditTextListeners()

        when (theme.inputMode) {
            PrimerTheme.InputMode.UNDERLINED -> {
                val res = context.resources
                val horizontalPadding =
                    res
                        .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_horizontal)
                cvvView.setPadding(horizontalPadding, 0, horizontalPadding, 0)
            }

            PrimerTheme.InputMode.OUTLINED -> Unit
        }
        validateCvvInput()
    }

    private fun validateCvvInput() {
        binding.vaultedPaymentMethodCvvCardCvvInput.doAfterTextChanged { editable ->
            primerViewModel.selectedSavedPaymentMethod?.let { token ->
                val data = token.paymentInstrumentData
                val network = requireNotNull(data.binData?.network) { "card data is invalid!" }
                val expectedLength = CardNetwork.lookupByCardNetwork(network).cvvLength
                binding.vaultedPaymentMethodCvvBtnSubmit.isEnabled = editable.toString().trim().length == expectedLength
            }
        }
    }

    private fun focusCvvInput() =
        FieldFocuser.focus(
            binding.vaultedPaymentMethodCvvCardCvvInput,
        ).also {
            adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
        }

    private fun logAnalyticsPresented() =
        primerViewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                action = AnalyticsAction.PRESENT,
                objectType = ObjectType.VIEW,
                place = Place.CVV_RECAPTURE_VIEW,
                context =
                primerViewModel.selectedSavedPaymentMethod?.paymentMethodType?.let {
                    PaymentMethodContextParams(it)
                },
            ),
        )

    private fun logAnalyticsBackPressed() =
        primerViewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                action = AnalyticsAction.CLICK,
                objectType = ObjectType.BUTTON,
                place = Place.CVV_RECAPTURE_VIEW,
                objectId = ObjectId.BACK,
                context =
                primerViewModel.selectedSavedPaymentMethod?.paymentMethodType?.let {
                    PaymentMethodContextParams(it)
                },
            ),
        )

    private fun logAnalyticsSubmitPressed() =
        primerViewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                action = AnalyticsAction.CLICK,
                objectType = ObjectType.BUTTON,
                place = Place.CVV_RECAPTURE_VIEW,
                objectId = ObjectId.SUBMIT,
                context =
                primerViewModel.selectedSavedPaymentMethod?.paymentMethodType?.let {
                    PaymentMethodContextParams(it)
                },
            ),
        )

    private fun logAnalyticsDismiss() =
        primerViewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                action = AnalyticsAction.DISMISS,
                objectType = ObjectType.VIEW,
                place = Place.CVV_RECAPTURE_VIEW,
                context =
                primerViewModel.selectedSavedPaymentMethod?.paymentMethodType?.let {
                    PaymentMethodContextParams(it)
                },
            ),
        )

    companion object {
        private const val ALPHA_HALF = 0.5f
        private const val ALPHA_VISIBLE = 1f

        fun newInstance(): VaultedPaymentMethodsCvvRecaptureFragment {
            return VaultedPaymentMethodsCvvRecaptureFragment()
        }
    }
}
