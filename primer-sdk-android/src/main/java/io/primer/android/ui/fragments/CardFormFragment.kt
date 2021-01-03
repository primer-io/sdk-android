package io.primer.android.ui.fragments

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import io.primer.android.R
import io.primer.android.UniversalCheckout
import io.primer.android.logging.Logger
import io.primer.android.model.dto.APIError
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
import org.json.JSONObject
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CardFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
internal class CardFormFragment : Fragment() {
  private val log = Logger("card-form")
  private lateinit var inputs: Map<String, TextInputEditText>
  private lateinit var submitButton: ViewGroup
  private lateinit var submitButtonText: TextView
  private lateinit var submitButtonLoading: ProgressBar
  private lateinit var errorText: TextView
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
  ): View {
    return inflater.inflate(R.layout.fragment_card_form, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Assign UI vars
    inputs = mapOf(
      CARD_NAME_FILED_NAME to view.findViewById(R.id.card_form_cardholder_name_input),
      CARD_NUMBER_FIELD_NAME to view.findViewById(R.id.card_form_card_number_input),
      CARD_EXPIRY_FIELD_NAME to view.findViewById(R.id.card_form_card_expiry_input),
      CARD_CVV_FIELD_NAME to view.findViewById(R.id.card_form_card_cvv_input),
    )
    submitButton = view.findViewById(R.id.card_form_submit_button)
    submitButtonText = view.findViewById(R.id.card_form_submit_button_txt)
    submitButtonLoading = view.findViewById(R.id.card_form_submit_button_loading)
    errorText = view.findViewById(R.id.card_form_error_message)

    // Attach view model observers
    tokenizationViewModel.status.observe(viewLifecycleOwner, this::onStatusChanged)
    tokenizationViewModel.error.observe(viewLifecycleOwner, this::onErrorChanged)
    tokenizationViewModel.result.observe(viewLifecycleOwner, this::onResultChanged)

    tokenizationViewModel.validationErrors.observe(viewLifecycleOwner, {
      setValidationErrors()
    })
    tokenizationViewModel.submitted.observe(viewLifecycleOwner, {
      setValidationErrors()
    })

    viewModel.keyboardVisible.observe(viewLifecycleOwner, this::onKeyboardVisibilityChanged)
    viewModel.uxMode.observe(viewLifecycleOwner, this::onUXModeChanged)

    tokenizationViewModel.reset(viewModel.selectedPaymentMethod.value)

    // Attach input event listeners

    // input masks
    inputs[CARD_EXPIRY_FIELD_NAME]?.addTextChangedListener(TextInputMask.ExpiryDate())
    inputs[CARD_NUMBER_FIELD_NAME]?.addTextChangedListener(TextInputMask.CardNumber())

    // text change listeners
    inputs.entries.forEach {
      it.value.addTextChangedListener(createTextWatcher(it.key))
    }

    // Click listeners
    submitButton.setOnClickListener {
      tokenizationViewModel.tokenize()
    }

    // IME action listeners
    inputs[CARD_CVV_FIELD_NAME]?.setOnEditorActionListener { v, c, e ->
      submitButton.performClick()
    }

    // grab focus to display the keyboard
    focusFirstInput()
  }

  private fun toggleLoading(on: Boolean) {
    if (on) {
      errorText.visibility = View.INVISIBLE
      submitButtonLoading.visibility = View.VISIBLE
    } else {
      submitButtonLoading.visibility = View.GONE
    }
  }

  private fun focusFirstInput() {
    val input = inputs[CARD_NAME_FILED_NAME] ?: return
    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    input.requestFocus()
    imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
  }

  private fun createTextWatcher(name: String): TextWatcher {
    return object : TextWatcher {
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

  private fun onStatusChanged(status: TokenizationStatus) {
    when (status) {
      TokenizationStatus.LOADING -> toggleLoading(true)
      else -> toggleLoading(false)
    }
  }

  private fun onErrorChanged(error: APIError?) {
    if (error == null) {
      return
    }

    errorText.text = requireContext().getText(R.string.payment_method_error)
    errorText.visibility = View.VISIBLE
  }

  private fun onResultChanged(data: JSONObject?) {
    if (data != null) {
      if (viewModel.uxMode.value == UniversalCheckout.UXMode.ADD_PAYMENT_METHOD) {
        viewModel.viewStatus.value = ViewStatus.VIEW_VAULTED_PAYMENT_METHODS
      }
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

  private fun onUXModeChanged(mode: UniversalCheckout.UXMode) {
    submitButtonText.text = when (mode) {
      UniversalCheckout.UXMode.ADD_PAYMENT_METHOD -> requireContext().getString(R.string.add_card)
      UniversalCheckout.UXMode.CHECKOUT -> PayAmountText.generate(
        requireContext(),
        viewModel.amount.value
      )
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