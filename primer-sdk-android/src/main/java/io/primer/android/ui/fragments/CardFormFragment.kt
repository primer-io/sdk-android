package io.primer.android.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import io.primer.android.R
import io.primer.android.UniversalCheckout
import io.primer.android.logging.Logger
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.card.CARD_CVV_FIELD_NAME
import io.primer.android.payment.card.CARD_EXPIRY_FIELD_NAME
import io.primer.android.payment.card.CARD_NAME_FILED_NAME
import io.primer.android.payment.card.CARD_NUMBER_FIELD_NAME
import io.primer.android.ui.PayAmountText
import io.primer.android.ui.TextInputMask
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationStatus
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.ViewStatus
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CardFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
internal class CardFormFragment : Fragment() {
  private val log = Logger("card-form")
  private lateinit var inputs: Map<String, TextInputEditText>
  private lateinit var submitButton: Button
  private lateinit var viewModel: PrimerViewModel
  private lateinit var tokenizationViewModel: TokenizationViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel = PrimerViewModel.getInstance(requireActivity())
    tokenizationViewModel = TokenizationViewModel.getInstance(requireActivity())
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_card_form, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    tokenizationViewModel.status.observe(viewLifecycleOwner, {
      if (it == TokenizationStatus.SUCCESS) {
        onSuccess()
      }
    })

    tokenizationViewModel.error.observe(viewLifecycleOwner, {
      if (it != null) {
        // TODO: handle error message
      }
    })

    tokenizationViewModel.result.observe(viewLifecycleOwner, {
      if (it != null) {
        if (viewModel.uxMode.value == UniversalCheckout.UXMode.ADD_PAYMENT_METHOD) {
          viewModel.viewStatus.value = ViewStatus.VIEW_VAULTED_PAYMENT_METHODS
        }
      }
    })

    viewModel.keyboardVisible.observe(viewLifecycleOwner, { visible ->
      val hasFocus = inputs.entries.any { it.value.isFocused }

      if (hasFocus && !visible) {
        inputs.entries.forEach {
          it.value.clearFocus()
        }
      } else if (visible && !hasFocus) {
        focusFirstInput()
      }
    })

    viewModel.uxMode.observe(viewLifecycleOwner, {
      submitButton.text = when (it) {
        UniversalCheckout.UXMode.ADD_PAYMENT_METHOD -> requireContext().getString(R.string.add_card)
        UniversalCheckout.UXMode.CHECKOUT -> PayAmountText.generate(requireContext(), viewModel.amount.value)
        else -> ""
      }
    })

    tokenizationViewModel.validationErrors.observe(viewLifecycleOwner, {
      setValidationErrors()
    })
    tokenizationViewModel.submitted.observe(viewLifecycleOwner, {
      setValidationErrors()
    })

    tokenizationViewModel.reset(viewModel.selectedPaymentMethod.value)

    inputs = mapOf(
      CARD_NAME_FILED_NAME to view.findViewById(R.id.card_form_cardholder_name_input),
      CARD_NUMBER_FIELD_NAME to view.findViewById(R.id.card_form_card_number_input),
      CARD_EXPIRY_FIELD_NAME to view.findViewById(R.id.card_form_card_expiry_input),
      CARD_CVV_FIELD_NAME to view.findViewById(R.id.card_form_card_cvv_input),
    )

    inputs[CARD_EXPIRY_FIELD_NAME]?.addTextChangedListener(TextInputMask.ExpiryDate())
    inputs[CARD_NUMBER_FIELD_NAME]?.addTextChangedListener(TextInputMask.CardNumber())

    inputs.entries.forEach {
      it.value.addTextChangedListener(createTextWatcher(it.key))
    }

    submitButton = view.findViewById(R.id.card_form_submit_button)

    submitButton.setOnClickListener {
      tokenizationViewModel.tokenize()
    }

    focusFirstInput()

    inputs[CARD_CVV_FIELD_NAME]?.setOnEditorActionListener { v, c, e ->
      submitButton.performClick()
    }
  }

  private fun onSuccess() {
    // TODO: handle checkout flow here
    viewModel.viewStatus.value = ViewStatus.VIEW_VAULTED_PAYMENT_METHODS
  }

  private fun focusFirstInput() {
    val input = inputs[CARD_NAME_FILED_NAME] ?: return

    input.requestFocus()

    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
  }

  private fun createTextWatcher(name: String) : TextWatcher {
    log("Add textWatcher to $name")
    return object: TextWatcher {
      override fun afterTextChanged(s: Editable?) {}

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        tokenizationViewModel.setTokenizableValue(name, s.toString())
      }
    }
  }

  private fun setValidationErrors() {
    val errors = tokenizationViewModel.validationErrors.value ?: Collections.emptyList()

    submitButton.isEnabled = errors.isEmpty()

    if (tokenizationViewModel.submitted.value != true) {
      return
    }

    inputs.entries.forEach {
      setValidationErrorState(it.value, errors.find { err -> err.name == it.key })
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

  companion object {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CardFormFragment.
     */
    @JvmStatic
    fun newInstance(): CardFormFragment {
      return CardFormFragment()
    }
  }
}