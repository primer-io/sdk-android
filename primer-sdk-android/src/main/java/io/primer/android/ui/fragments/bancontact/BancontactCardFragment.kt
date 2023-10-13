package io.primer.android.ui.fragments.bancontact

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import io.primer.android.R
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.databinding.FragmentFormBancontactCardBinding
import io.primer.android.di.extension.activityViewModel
import io.primer.android.di.extension.inject
import io.primer.android.di.extension.viewModel
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.CardNetwork
import io.primer.android.ui.PayAmountText
import io.primer.android.ui.TextInputMask
import io.primer.android.ui.components.TextInputWidget
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.utils.hideKeyboard
import io.primer.android.utils.sanitized
import io.primer.android.viewmodel.TokenizationStatus
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.TokenizationViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.reflect.KFunction1

@ExperimentalCoroutinesApi
internal class BancontactCardFragment : BaseFragment() {

    private val localConfig: PrimerConfig by inject()

    private val tokenizationViewModel: TokenizationViewModel by
    activityViewModel<TokenizationViewModel, TokenizationViewModelFactory>()
    private val viewModel: BancontactCardViewModel by
    viewModel<BancontactCardViewModel, BancontactCardViewModelFactory>()

    private var binding: FragmentFormBancontactCardBinding by autoCleaned()

    private val inputViews: MutableList<TextInputWidget> = mutableListOf()
    private var network: CardNetwork.Descriptor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFormBancontactCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTokenizeObserver()
        setupComponents()
        setupTheme()
        setupInputs()
        setupListeners()
    }

    private fun setupTokenizeObserver() {
        tokenizationViewModel.tokenizationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                TokenizationStatus.LOADING -> binding.btnPay.showProgress()
                else -> binding.btnPay.hideProgress()
            }
        }
    }

    private fun setupComponents() {
        inputViews.add(binding.cardFormCardNumber)
        inputViews.add(binding.cardFormCardExpiry)
        inputViews.add(binding.cardFormCardholderName)

        updateCardNumberInputIcon()
        updateSubmitButton()
    }

    private fun setupTheme() {
        val imageColorStates = ColorStateList.valueOf(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.ivBack.imageTintList = imageColorStates

        inputViews.forEach { inputView ->
            val fontSize = theme.input.text.fontSize.getDimension(requireContext())
            inputView.editText?.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)

            val color = theme.input.text.defaultColor.getColor(requireContext(), theme.isDarkMode)
            inputView.editText?.setTextColor(color)
            inputView.setupEditTextTheme()
            inputView.setupEditTextListeners()

            when (theme.inputMode) {
                PrimerTheme.InputMode.UNDERLINED -> setInputFieldPadding(inputView)
                PrimerTheme.InputMode.OUTLINED -> Unit
            }
        }
    }

    private fun setInputFieldPadding(view: View) {
        val res = requireContext().resources
        val horizontalPadding = res
            .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_horizontal)

        view.setPadding(horizontalPadding, 0, horizontalPadding, 0)
    }

    private fun setupInputs() {
        binding.cardFormCardExpiry.editText?.addTextChangedListener(
            TextInputMask.ExpiryDate()
        )
        binding.cardFormCardNumber.editText?.addTextChangedListener(
            TextInputMask.CardNumber()
        )
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.btnPay.setOnClickListener {
            it.hideKeyboard()
            validateAllFields()
            if (isReadyForPAy()) {
                val descriptor =
                    primerViewModel.selectedPaymentMethod.value as AsyncPaymentMethodDescriptor

                viewModel.collectData().forEach { input ->
                    descriptor.setTokenizableValue(input.first, input.second)
                }

                descriptor.behaviours.forEach {
                    primerViewModel.executeBehaviour(it)
                }
            }
        }

        binding.cardFormCardNumber.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveAndValidateContent(
                    binding.cardFormCardNumber.editText?.text,
                    viewModel::onUpdateCardNumberInput,
                    binding.cardFormCardNumber,
                    R.string.card_number
                )
            }
        }
        binding.cardFormCardExpiry.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveAndValidateContent(
                    binding.cardFormCardExpiry.editText?.text,
                    viewModel::onUpdateCardExpiry,
                    binding.cardFormCardExpiry,
                    R.string.card_expiry
                )
            }
        }
        binding.cardFormCardholderName.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveAndValidateContent(
                    binding.cardFormCardholderName.editText?.text,
                    viewModel::onUpdateCardholderName,
                    binding.cardFormCardholderName,
                    R.string.card_holder_name
                )
            }
        }

        binding.cardFormCardNumber.editText?.addTextChangedListener(
            afterTextChanged = ::onCardNumberInput
        )

        binding.cardFormCardExpiry.editText?.addTextChangedListener {
            binding.cardFormCardExpiry.removeError()
            updateSubmitButton()
        }

        binding.cardFormCardholderName.editText?.addTextChangedListener {
            binding.cardFormCardholderName.removeError()
            updateSubmitButton()
        }
    }

    private fun saveAndValidateContent(
        content: Editable?,
        onValidateAndSave: KFunction1<String, Int?>,
        inputWidget: TextInputWidget,
        inputNameResId: Int
    ) {
        onValidateAndSave.invoke(content.toString().sanitized()).let { invalidMsgId ->
            val errorMsg = if (invalidMsgId == null) {
                null
            } else {
                getString(
                    invalidMsgId,
                    getString(inputNameResId)
                )
            }
            inputWidget.error = errorMsg
            inputWidget.isErrorEnabled = errorMsg != null
        }
    }

    private fun onCardNumberInput(content: Editable?) {
        val newNetwork = CardNetwork.lookup(content.toString())
        val isSameNetwork = network?.type?.equals(newNetwork.type) ?: false
        binding.cardFormCardNumber.removeError()
        if (isSameNetwork) return

        network = newNetwork

        updateCardNumberInputIcon()
        updateSubmitButton()
    }

    private fun validateAllFields() {
        saveAndValidateContent(
            binding.cardFormCardNumber.editText?.text,
            viewModel::onUpdateCardNumberInput,
            binding.cardFormCardNumber,
            R.string.card_number
        )
        saveAndValidateContent(
            binding.cardFormCardExpiry.editText?.text,
            viewModel::onUpdateCardExpiry,
            binding.cardFormCardExpiry,
            R.string.card_expiry
        )
        saveAndValidateContent(
            binding.cardFormCardholderName.editText?.text,
            viewModel::onUpdateCardholderName,
            binding.cardFormCardholderName,
            R.string.card_holder_name
        )
    }

    private fun updateSubmitButton() {
        val amount = localConfig.getMonetaryAmountWithSurcharge()
        val amountString = PayAmountText.generate(requireContext(), amount)
        binding.btnPay.text = getString(R.string.pay_specific_amount, amountString)

        binding.btnPay.isEnabled = isReadyForPAy()
    }

    private fun isReadyForPAy(): Boolean {
        return !binding.cardFormCardNumber.isErrorEnabled &&
            !binding.cardFormCardExpiry.isErrorEnabled &&
            !binding.cardFormCardholderName.isErrorEnabled
    }

    private fun updateCardNumberInputIcon() {
        val resource = network?.getResource() ?: R.drawable.ic_generic_card
        binding.cardFormCardNumber.editText?.setCompoundDrawablesRelativeWithIntrinsicBounds(
            resource,
            0,
            0,
            0
        )
    }

    companion object {

        fun newInstance() = BancontactCardFragment()
    }
}
