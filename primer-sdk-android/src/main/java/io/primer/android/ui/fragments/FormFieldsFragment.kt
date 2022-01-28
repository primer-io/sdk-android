package io.primer.android.ui.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
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
import io.primer.android.databinding.FragmentFormFieldsBinding
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.FormField
import io.primer.android.ui.extensions.autoCleaned

private const val BOTTOM_MARGIN = 16

internal class FormFieldsFragment : FormChildFragment() {

    private var binding: FragmentFormFieldsBinding by autoCleaned()
    private val fieldIds: MutableMap<String, Int> = HashMap()

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
                bottomMargin = BOTTOM_MARGIN
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

                inputField.backgroundTintList = ColorStateList.valueOf(Color.RED)

                val hintText = ctx.getString(field.labelId)

                inputLayout.hint = hintText
                inputLayout.layoutParams = createLayoutParams()

                return inputLayout
            }

            private fun listenerToTextWatcher(l: InputChangeListener): TextWatcher {
                return object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        // no op
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        l.onValueChange(s.toString())
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // no op
                    }
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
                    l.onValueChange(picker.selectedCountryNameCode)
                }

                inputLayout.layoutParams = createLayoutParams()

                return inputLayout
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFormFieldsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        val inputLayout =
                            binding.fragmentFormFields.findViewById<TextInputLayout>(id)
                        val inputField = inputLayout.findViewById<TextInputEditText>(
                            R.id.form_input_field
                        )

                        inputLayout.boxStrokeErrorColor = ColorStateList.valueOf(Color.RED)

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
        binding.fragmentFormFields.removeAllViews()
    }

    private fun hideView() {
        clearViews()
        binding.fragmentFormFields.visibility = View.GONE
    }

    private fun showFields(items: List<FormField>) {
        clearViews()
        binding.fragmentFormFields.visibility = View.VISIBLE

        var focused: View? = null

        for (field in items) {
            val view = createInput(field)
            if (focused == null && field.autoFocus) {
                focused = view
            }
            binding.fragmentFormFields.addView(view)
        }

        binding.fragmentFormFields.requestLayout()

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
