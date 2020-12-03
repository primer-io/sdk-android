package io.primer.android.payment.card

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import io.primer.android.R
import org.w3c.dom.Text

/**
 * A simple [Fragment] subclass.
 * Use the [CardFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CardFormFragment : Fragment() {
  private lateinit var cardholderNameInput: TextInputEditText
  private lateinit var cardNumberInput: TextInputEditText
  private lateinit var cardExpiryInput: TextInputEditText
  private lateinit var cardCvvInput: TextInputEditText
  private lateinit var submitButton: Button

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_card_form, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    cardholderNameInput = view.findViewById(R.id.card_form_cardholder_name_input)
    cardNumberInput = view.findViewById(R.id.card_form_card_number_input)
    cardExpiryInput = view.findViewById(R.id.card_form_card_expiry_input)
    cardCvvInput = view.findViewById(R.id.card_form_card_cvv_input)
    submitButton = view.findViewById(R.id.card_form_submit_button)

    focusInput(cardholderNameInput)
  }

  private fun focusInput(input: View) {
    input.requestFocus()

    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
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