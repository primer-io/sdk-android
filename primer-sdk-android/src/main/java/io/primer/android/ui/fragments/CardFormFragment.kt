package io.primer.android.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.textfield.TextInputEditText
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.PaymentMethodIntent
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.card.CARD_CVV_FIELD_NAME
import io.primer.android.payment.card.CARD_EXPIRY_FIELD_NAME
import io.primer.android.payment.card.CARD_NAME_FILED_NAME
import io.primer.android.payment.card.CARD_NUMBER_FIELD_NAME
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.PayAmountText
import io.primer.android.ui.TextInputMask
import io.primer.android.ui.components.ButtonPrimary
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationStatus
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import java.util.Collections
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 * Use the [CardFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@KoinApiExtension
internal class CardFormFragment : Fragment(), DIAppComponent {

    private lateinit var inputs: Map<String, TextInputEditText>
    private lateinit var submitButton: ButtonPrimary
    private lateinit var cancelButton: TextView
    private lateinit var title: TextView

    private lateinit var errorText: TextView

    private val primerViewModel: PrimerViewModel by activityViewModels()
    private val tokenizationViewModel: TokenizationViewModel by viewModel()
    private val checkoutConfig: PrimerConfig by inject()
    private val theme: PrimerTheme by inject()
    private val dirtyMap: MutableMap<String, Boolean> = HashMap()
    private var firstMount: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_card_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inputs = mapOf(
            CARD_NAME_FILED_NAME to view.findViewById(R.id.card_form_cardholder_name_input),
            CARD_NUMBER_FIELD_NAME to view.findViewById(R.id.card_form_card_number_input),
            CARD_EXPIRY_FIELD_NAME to view.findViewById(R.id.card_form_card_expiry_input),
            CARD_CVV_FIELD_NAME to view.findViewById(R.id.card_form_card_cvv_input),
        )

        inputs.values.forEach { t ->
            val fontSize = theme.input.text.fontsize.getDimension(requireContext())
            t.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
            t.setTextColor(
                theme.input.text.defaultColor.getColor(
                    requireContext(),
                    theme.isDarkMode
                )
            )
            when (theme.inputMode) {
                PrimerTheme.InputMode.UNDERLINED -> {
                    val res = requireContext().resources
                    val topPadding = res
                        .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_top)
                    val horizontalPadding = res
                        .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_horizontal)
                    val bottomPadding = res
                        .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_bottom)
                    t.setPadding(horizontalPadding, topPadding, horizontalPadding, bottomPadding)
                }
                PrimerTheme.InputMode.OUTLINED -> Unit
            }
        }

        submitButton = view.findViewById(R.id.card_form_submit_button)
        cancelButton = view.findViewById(R.id.nav_cancel_button)
        errorText = view.findViewById(R.id.card_form_error_message)
        title = view.findViewById(R.id.card_form_title)

        tokenizationViewModel.tokenizationStatus.observe(viewLifecycleOwner) { status ->
            val loading = when (status) {
                TokenizationStatus.LOADING, TokenizationStatus.SUCCESS -> true
                else -> false
            }
            toggleLoading(loading)
            enableSubmitButton(isSubmitButtonEnabled(status))
        }
        tokenizationViewModel.tokenizationError.observe(viewLifecycleOwner) {
            errorText.text = requireContext().getText(R.string.payment_method_error)
            errorText.visibility = View.VISIBLE
        }

        tokenizationViewModel.validationErrors.observe(
            viewLifecycleOwner,
            {
                setValidationErrors(tokenizationViewModel.tokenizationStatus.value)
            }
        )
        tokenizationViewModel.submitted.observe(
            viewLifecycleOwner,
            {
                setValidationErrors(tokenizationViewModel.tokenizationStatus.value)
            }
        )

        primerViewModel.keyboardVisible.observe(viewLifecycleOwner, ::onKeyboardVisibilityChanged)
        onUXModeChanged(checkoutConfig.paymentMethodIntent)

        tokenizationViewModel.resetPaymentMethod(primerViewModel.selectedPaymentMethod.value)

        inputs[CARD_EXPIRY_FIELD_NAME]?.addTextChangedListener(TextInputMask.ExpiryDate())
        inputs[CARD_NUMBER_FIELD_NAME]?.addTextChangedListener(TextInputMask.CardNumber())

        inputs.entries.forEach {
            it.value.addTextChangedListener(createTextWatcher(it.key))
            it.value.onFocusChangeListener = createFocusChangeListener(it.key)
        }

        submitButton.setOnClickListener {
            if (tokenizationViewModel.isValid()) {
                tokenizationViewModel.tokenize()
            }
        }

        cancelButton.setTextColor(
            theme.systemText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        cancelButton.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            theme.systemText.fontsize.getDimension(requireContext()),
        )
        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        inputs[CARD_NAME_FILED_NAME]?.setOnEditorActionListener { _, _, _ ->
            submitButton.performClick()
        }

        renderTitle()

        focusFirstInput()
    }

    private fun renderTitle() {
        title.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
    }

    private fun toggleLoading(on: Boolean) {
        submitButton.setProgress(on)
        if (on) {
            errorText.visibility = View.INVISIBLE
        }
    }

    private fun enableSubmitButton(enabled: Boolean) {
        submitButton.isEnabled = enabled
    }

    private fun focusFirstInput() {
        FieldFocuser.focus(inputs[CARD_NUMBER_FIELD_NAME])
    }

    private fun createTextWatcher(name: String): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // no op
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tokenizationViewModel.setTokenizableValue(name, s.toString())
            }
        }
    }

    private fun createFocusChangeListener(name: String): View.OnFocusChangeListener {
        return View.OnFocusChangeListener { _, hasFocus ->
            var skip = false

            if (!hasFocus && firstMount && name == CARD_NUMBER_FIELD_NAME) {
                firstMount = false
                skip = true
            }

            if (!skip && !hasFocus) {
                dirtyMap[name] = true
            }

            setValidationErrors(tokenizationViewModel.tokenizationStatus.value)
        }
    }

    private fun setValidationErrors(tokenizationStatus: TokenizationStatus?) {
        val errors = tokenizationViewModel.validationErrors.value ?: Collections.emptyList()

        submitButton.isEnabled = errors.isEmpty() && isSubmitButtonEnabled(tokenizationStatus)

        val showAll = tokenizationViewModel.submitted.value == true

        inputs.entries.forEach {
            val dirty = getIsDirty(it.key)
            val focused = it.value.isFocused

            if (showAll || dirty) {
                setValidationErrorState(
                    it.value,
                    if (focused) null else errors.find { err -> err.name == it.key }
                )
            }
        }
    }

    private fun setValidationErrorState(input: TextInputEditText, error: SyncValidationError?) {

        if (error == null) {
            input.error = null
        } else {
            val ctx = requireContext()
            input.error = ctx.getString(error.errorId, ctx.getString(error.fieldId))
        }
    }

    private fun onKeyboardVisibilityChanged(visible: Boolean) {
        val hasFocus = inputs.entries.any { it.value.isFocused }

        if (hasFocus && !visible) {
            inputs.entries.forEach {
                it.value.clearFocus()
            }
        } else if (visible && !hasFocus) {
            focusFirstInput()
        }
    }

    private fun onUXModeChanged(mode: PaymentMethodIntent) {
        submitButton.text = when (mode) {
            PaymentMethodIntent.VAULT -> requireContext().getString(R.string.add_card)
            PaymentMethodIntent.CHECKOUT -> String.format(
                requireContext().getString(R.string.pay_specific_amount),
                PayAmountText.generate(requireContext(), checkoutConfig.monetaryAmount),
            )
        }
    }

    private fun getIsDirty(name: String): Boolean {
        return dirtyMap[name] ?: false
    }

    private fun isSubmitButtonEnabled(tokenizationStatus: TokenizationStatus?): Boolean {
        return tokenizationStatus == TokenizationStatus.NONE ||
            tokenizationStatus == TokenizationStatus.ERROR
    }

    companion object {

        @JvmStatic
        fun newInstance(): CardFormFragment {
            return CardFormFragment()
        }
    }
}
