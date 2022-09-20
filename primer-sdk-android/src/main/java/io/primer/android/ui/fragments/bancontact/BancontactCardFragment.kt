package io.primer.android.ui.fragments.bancontact

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.databinding.FragmentFormBancontactCardBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.CardNetwork
import io.primer.android.ui.PayAmountText
import io.primer.android.ui.TextInputMask
import io.primer.android.ui.components.TextInputWidget
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.utils.sanitized
import io.primer.android.viewmodel.PrimerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.inject

@ExperimentalCoroutinesApi
internal class BancontactCardFragment : Fragment(), DIAppComponent {
    private val theme: PrimerTheme by inject()
    private val localConfig: PrimerConfig by inject()

    private val primerViewModel: PrimerViewModel by activityViewModels()
    private val viewModel: BancontactCardViewModel by viewModel()

    private var binding: FragmentFormBancontactCardBinding by autoCleaned()

    private val inputViews: MutableList<TextInputWidget> = mutableListOf()
    private var network: CardNetwork.Descriptor? = null
    private val networkAsString: String get() = network?.type.toString()

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
        setupComponents()
        setupTheme()
        setupInputs()
        setupListeners()
    }

    private fun setupComponents() {
        inputViews.add(binding.cardFormCardNumber)
        inputViews.add(binding.cardFormCardExpiry)
        inputViews.add(binding.cardFormCardholderName)

        updateCardNumberInputIcon()
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
            val descriptor =
                primerViewModel.selectedPaymentMethod.value as AsyncPaymentMethodDescriptor

            viewModel.collectData().forEach { input ->
                descriptor.setTokenizableValue(input.first, input.second)
            }

            descriptor.behaviours.forEach {
                primerViewModel.executeBehaviour(it)
            }
        }

        binding.cardFormCardNumber.editText?.addTextChangedListener(
            afterTextChanged = ::onCardNumberInput
        )
        binding.cardFormCardExpiry.editText?.addTextChangedListener {
            viewModel.onUpdateCardExpiry(it.toString().sanitized())
        }
        binding.cardFormCardholderName.editText?.addTextChangedListener {
            viewModel.onUpdateCardholderName(it.toString().sanitized())
        }
    }

    private fun onCardNumberInput(content: Editable?) {
        val newNetwork = CardNetwork.lookup(content.toString())
        val isSameNetwork = network?.type?.equals(newNetwork.type) ?: false
        viewModel.onUpdateCardNumberInput(content.toString().sanitized())
        if (isSameNetwork) return

        network = newNetwork

        updateCardNumberInputIcon()
        emitCardNetworkAction()
    }

    private fun emitCardNetworkAction() {
        val actionParams = if (network == null || network?.type == CardNetwork.Type.UNKNOWN) {
            ActionUpdateUnselectPaymentMethodParams
        } else {
            ActionUpdateSelectPaymentMethodParams(
                PaymentMethodType.PAYMENT_CARD.name,
                networkAsString
            )
        }

        primerViewModel.dispatchAction(actionParams) {
            lifecycleScope.launchWhenStarted {
                updateSubmitButton()
            }
        }
    }

    private fun updateSubmitButton() {
        val amount = localConfig.getMonetaryAmountWithSurcharge()
        val amountString = PayAmountText.generate(requireContext(), amount)
        binding.btnPay.text = getString(R.string.pay_specific_amount, amountString)
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
