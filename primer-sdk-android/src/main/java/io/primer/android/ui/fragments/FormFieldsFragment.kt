package io.primer.android.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hbb20.CountryCodePicker
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.FormField

internal class FormFieldsFragment : FormChildFragment() {
  private lateinit var layout: ViewGroup
  private val fieldIds: MutableMap<String, Int> = HashMap()
  private val log = Logger("form-fields")

  interface InputChangeListener {
    fun onValueChange(text: String)
  }

  private sealed class InputElementFactory(protected val field: FormField) {
    abstract fun create(ctx: Context, l: InputChangeListener, initialValue: String): View

    protected fun createLayoutParams(): LinearLayout.LayoutParams {
      return LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT
      ).apply {
        bottomMargin = 16
      }
    }

    class EditText(field: FormField) : InputElementFactory(field) {
      override fun create(ctx: Context, l: InputChangeListener, initialValue: String): View {

        val id = View.generateViewId()
        val inputLayout = View.inflate(ctx, R.layout.form_input, null) as TextInputLayout
        val inputField = inputLayout.findViewById<TextInputEditText>(R.id.form_input_field)

        inputLayout.id = id

        inputField.inputType = getInputType(field.inputType)
        inputField.addTextChangedListener(listenerToTextWatcher(l))
        inputField.text = SpannableStringBuilder(initialValue)

        val hintText = ctx.getString(field.labelId)

        inputLayout.hint = hintText
        inputLayout.layoutParams = createLayoutParams()

        return inputLayout
      }

      private fun listenerToTextWatcher(l: InputChangeListener): TextWatcher {
        return object : TextWatcher {
          override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

          override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            l.onValueChange(s.toString())
          }

          override fun afterTextChanged(s: Editable?) {}
        }
      }

      private fun getInputType(type: FormField.Type): Int {
        return when (type) {
          FormField.Type.TEXT -> InputType.TYPE_CLASS_TEXT
          FormField.Type.EMAIL -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
          FormField.Type.PERSON_NAME -> InputType.TYPE_TEXT_VARIATION_PERSON_NAME
          FormField.Type.POSTAL_ADDRESS -> InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
          else -> InputType.TYPE_CLASS_TEXT
        }
      }
    }

    class CountryCode(field: FormField) : InputElementFactory(field) {
      override fun create(ctx: Context, l: InputChangeListener, initialValue: String): View {
        val id = View.generateViewId()
        val inputLayout = View.inflate(ctx, R.layout.country_code_input, null)
        val picker = inputLayout.findViewById<CountryCodePicker>(R.id.country_code_picker)
        val label = inputLayout.findViewById<TextView>(R.id.country_code_label)

        inputLayout.id = id

        label.text = ctx.getString(field.labelId)
        picker.setDefaultCountryUsingNameCode(initialValue)
        picker.setCountryForNameCode(initialValue)

        picker.setOnCountryChangeListener {
          l.onValueChange(picker.selectedCountryCode)
        }

        inputLayout.layoutParams = createLayoutParams()

        return inputLayout
      }
    }
  }

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

    viewModel.fields.observe(viewLifecycleOwner) {
      if (it.isEmpty()) {
        hideView()
      } else {
        showFields(it)
      }
    }

    viewModel.validationErrors.observe(viewLifecycleOwner) { errors ->
      if (viewModel.submitted.value == true) {
        errors.forEach {
          val key = it.key
          val value = it.value

          fieldIds[key]?.let { id ->
            val inputLayout = layout.findViewById<TextInputLayout>(id)
            val inputField = inputLayout.findViewById<TextInputEditText>(R.id.form_input_field)

            inputField.error = if (value == null) null else requireContext().getString(
              value.errorId,
              requireContext().getString(value.fieldId)
            )
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

    layout.requestLayout()

    FieldFocuser.focus(focused?.findViewById(R.id.form_input_field))
  }

  private fun createInput(field: FormField): View {
    val factory = getElementFactory(field)
    val element = factory.create(
      requireContext(),
      object : InputChangeListener {
        override fun onValueChange(text: String) {
          viewModel.setValue(field.name, text)
        }
      },
      viewModel.getValue(field.name)
    )

    fieldIds[field.name] = element.id

    return element
  }


  private fun getElementFactory(field: FormField): InputElementFactory {
    return when (field.inputType) {
      FormField.Type.COUNTRY_CODE -> InputElementFactory.CountryCode(field)
      else -> InputElementFactory.EditText(field)
    }
  }

}