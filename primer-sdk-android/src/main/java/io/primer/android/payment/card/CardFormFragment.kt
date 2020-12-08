package io.primer.android.payment.card

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.ui.PayAmountText
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CardFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
internal class CardFormFragment : Fragment() {
  private val log = Logger("card-form")
//  private lateinit var cardholderNameInput: TextInputEditText
//  private lateinit var cardNumberInput: TextInputEditText
//  private lateinit var cardExpiryInput: TextInputEditText
//  private lateinit var cardCvvInput: TextInputEditText
  private lateinit var inputs: Map<String, TextInputEditText>
  private lateinit var submitButton: Button
  private lateinit var viewModel: PrimerViewModel
  private lateinit var tokenizationViewModel: TokenizationViewModel

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_card_form, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel = PrimerViewModel.getInstance(requireActivity())
    tokenizationViewModel = TokenizationViewModel.getInstance(requireActivity())


    tokenizationViewModel.status.observe(viewLifecycleOwner, {
      log("Tokenization status changed: ${it.name}")
    })

    tokenizationViewModel.error.observe(viewLifecycleOwner, {
      if (it != null) {
        log("Tokenization error: ${it.description}")
      }
    })

    tokenizationViewModel.result.observe(viewLifecycleOwner, {
      if (it != null) {
        log("Tokenization result: ${it.toString()}")
      }
    })

    tokenizationViewModel.validationErrors.observe(viewLifecycleOwner, {
      log("Validation errors changed!")
      log(it.toString())
    })

    viewModel.uxMode.observe(viewLifecycleOwner, {
      submitButton.setText(PayAmountText.generate(
        requireContext(), viewModel.uxMode.value, viewModel.amount.value)
      )
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

    inputs.entries.forEach {
      it.value.addTextChangedListener(createTextWatcher(it.key))
    }

    submitButton = view.findViewById(R.id.card_form_submit_button)

    submitButton.setOnClickListener {
      tokenizationViewModel.tokenize()
    }

    focusInput(inputs[CARD_NAME_FILED_NAME]!!)
  }

  private fun focusInput(input: View) {
    input.requestFocus()

    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
  }

  private fun createTextWatcher(name: String) : TextWatcher {
    log("Add textWatcher to $name")
    return object: TextWatcher {
      override fun afterTextChanged(s: Editable?) {
        log("afterTextChanged: $name : ${s.toString()}")
      }

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        log("beforeTextChanged: $name : ${s.toString()}")
      }

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        log("onTextChanged: $name : ${s.toString()}")

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