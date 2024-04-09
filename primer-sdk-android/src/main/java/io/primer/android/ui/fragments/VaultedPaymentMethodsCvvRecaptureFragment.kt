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
import io.primer.android.components.ui.assets.ImageColor
import io.primer.android.databinding.FragmentVaultedPaymentMethodCvvRecaptureBinding
import io.primer.android.di.extension.activityViewModel
import io.primer.android.ui.CardNetwork
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.utils.hideKeyboard
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.PrimerViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal class VaultedPaymentMethodsCvvRecaptureFragment : BaseFragment() {

    private var binding: FragmentVaultedPaymentMethodCvvRecaptureBinding by autoCleaned()

    private val viewModel: PrimerViewModel by
    activityViewModel<PrimerViewModel, PrimerViewModelFactory>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentVaultedPaymentMethodCvvRecaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
        binding.vaultedPaymentMethodCvvGoBack.setOnClickListener { view ->
            view.hideKeyboard()
            logAnalyticsBackPressed()
            viewModel.goToSelectPaymentMethodsView()
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

                val paymentMethod = viewModel.selectedSavedPaymentMethod ?: return@setOnClickListener
                val cvv = binding.vaultedPaymentMethodCvvCardCvvInput.text.toString().trim()
                viewModel.exchangePaymentMethodTokenWithAdditionalData(
                    paymentMethod,
                    cvv
                )
            }
        }
    }

    private fun setCardDetails() {
        val paymentMethod = viewModel.selectedSavedPaymentMethod ?: return
        val data = paymentMethod.paymentInstrumentData
        val last4: Int =
            data?.last4Digits ?: throw IllegalStateException("card data is invalid!")
        binding.vaultedPaymentMethodCvvLastFourLabel.text = getString(R.string.last_four, last4)
        binding.vaultedPaymentMethodCvvDescLabel.text = getString(
            R.string.primer_cvv_recapture_explanation,
            data.binData?.network?.let { CardNetwork.lookupByCardNetwork(it).cvvLength }
        )
        setCardIcon(data.binData?.network)
    }

    private fun setCardIcon(network: String?) = binding.vaultedPaymentMethodCvvCardIcon.apply {
        val resId = CardNetwork.Type.valueOrNull(network)?.getCardBrand()?.getImageAsset(ImageColor.COLORED)
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
        val context = requireContext()
        val titleView = binding.vaultedPaymentMethodCvvTitleLabel
        val textColor = theme.titleText.defaultColor.getColor(context, theme.isDarkMode)
        val fontSize = theme.titleText.fontSize.getDimension(context)
        titleView.setTextColor(textColor)
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
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
                val horizontalPadding = res
                    .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_horizontal)
                cvvView.setPadding(horizontalPadding, 0, horizontalPadding, 0)
            }

            PrimerTheme.InputMode.OUTLINED -> Unit
        }
        validateCvvInput()
    }

    private fun validateCvvInput() {
        binding.vaultedPaymentMethodCvvCardCvvInput.doAfterTextChanged { editable ->
            viewModel.selectedSavedPaymentMethod?.let { token ->
                val data = token.paymentInstrumentData
                val network = data?.binData?.network ?: throw IllegalStateException("card data is invalid!")
                val expectedLength = CardNetwork.lookupByCardNetwork(network).cvvLength
                binding.vaultedPaymentMethodCvvBtnSubmit.isEnabled = editable.toString().trim().length == expectedLength
            }
        }
    }

    private fun focusCvvInput() = FieldFocuser.focus(
        binding.vaultedPaymentMethodCvvCardCvvInput
    ).also {
        adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
    }

    private fun logAnalyticsPresented() = primerViewModel.addAnalyticsEvent(
        UIAnalyticsParams(
            action = AnalyticsAction.PRESENT,
            objectType = ObjectType.VIEW,
            place = Place.CVV_RECAPTURE_VIEW,
            context = primerViewModel.selectedSavedPaymentMethod?.paymentMethodType?.let {
                PaymentMethodContextParams(it)
            }
        )
    )

    private fun logAnalyticsBackPressed() = primerViewModel.addAnalyticsEvent(
        UIAnalyticsParams(
            action = AnalyticsAction.CLICK,
            objectType = ObjectType.BUTTON,
            place = Place.CVV_RECAPTURE_VIEW,
            objectId = ObjectId.BACK,
            context = primerViewModel.selectedSavedPaymentMethod?.paymentMethodType?.let {
                PaymentMethodContextParams(it)
            }
        )
    )

    private fun logAnalyticsSubmitPressed() = primerViewModel.addAnalyticsEvent(
        UIAnalyticsParams(
            action = AnalyticsAction.CLICK,
            objectType = ObjectType.BUTTON,
            place = Place.CVV_RECAPTURE_VIEW,
            objectId = ObjectId.SUBMIT,
            context = primerViewModel.selectedSavedPaymentMethod?.paymentMethodType?.let {
                PaymentMethodContextParams(it)
            }
        )
    )

    private fun logAnalyticsDismiss() = primerViewModel.addAnalyticsEvent(
        UIAnalyticsParams(
            action = AnalyticsAction.DISMISS,
            objectType = ObjectType.VIEW,
            place = Place.CVV_RECAPTURE_VIEW,
            context = primerViewModel.selectedSavedPaymentMethod?.paymentMethodType?.let {
                PaymentMethodContextParams(it)
            }
        )
    )

    companion object {

        private const val ALPHA_HALF = 0.5f
        private const val ALPHA_VISIBLE = 1f
        fun newInstance(): VaultedPaymentMethodsCvvRecaptureFragment {
            return VaultedPaymentMethodsCvvRecaptureFragment()
        }
    }
}
