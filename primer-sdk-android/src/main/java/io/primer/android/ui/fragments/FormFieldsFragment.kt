package io.primer.android.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.ui.FieldFocuser
import io.primer.android.viewmodel.FormField
import io.primer.android.viewmodel.FormViewModel

class FormFieldsFragment : Fragment() {
  private lateinit var viewModel: FormViewModel
  private lateinit var layout: ViewGroup
  private val fieldIds: MutableMap<String, Int> = HashMap()
  private val log = Logger("form-fields")

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_form_fields, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    layout = view.findViewById(R.id.fragment_form_fields)

    viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)

    viewModel.fields.observe(viewLifecycleOwner) {
      if (it.isEmpty()) {
        hideView()
      } else {
        showFields(it)
      }
    }

    viewModel.errors.observe(viewLifecycleOwner) { errors ->
      if (viewModel.meta.value?.submitted == true) {
        errors.forEach {
          val key = it.key
          val value = it.value

          fieldIds[key]?.let { id ->
            val inputLayout = layout.findViewById<TextInputLayout>(id)
            val inputField = inputLayout.findViewById<TextInputEditText>(R.id.form_input_field)

            if (value == null) {
              inputField.error = null
            } else {
              inputField.error = requireContext().getString(value.errorId, requireContext().getString(value.fieldId))
            }
          }
        }
      }
    }
  }

  private fun clearViews() {
    fieldIds.clear()
    layout.removeAllViews()
  }

  private fun hideView() {
    clearViews()
    layout.visibility = View.GONE
  }

  private fun showFields(items: List<FormField>) {
    clearViews()
    layout.visibility = View.VISIBLE

    var focused: View? = null

    for (field in items) {
      val view = createInput(field)
      if (focused == null && field.autoFocus) {
        focused = view
      }
      layout.addView(view)
    }

    FieldFocuser.focus(focused?.findViewById(R.id.form_input_field))
  }

  private fun createInput(field: FormField) : TextInputLayout {
    val id = View.generateViewId()
    val inputLayout = View.inflate(requireContext(), R.layout.form_input, null) as TextInputLayout
    val inputField = inputLayout.findViewById<TextInputEditText>(R.id.form_input_field)

    inputLayout.id = id
    fieldIds[field.name] = id

    inputField.inputType = field.inputType
    inputField.hint = requireContext().getString(field.labelId)
    inputField.addTextChangedListener(createWatcher(field.name))
    inputField.text = SpannableStringBuilder(viewModel.getValue(field.name))

    inputLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

    return inputLayout
  }

  private fun createWatcher(name: String): TextWatcher {
    return object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        /* no-op */
      }

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        viewModel.setFieldValue(name, s.toString())
      }

      override fun afterTextChanged(s: Editable?) {
        /* no-op */
      }

    }
  }
}