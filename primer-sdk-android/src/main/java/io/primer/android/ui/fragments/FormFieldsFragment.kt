package io.primer.android.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.primer.android.R
import io.primer.android.databinding.FragmentFormFieldsBinding
import io.primer.android.domain.action.models.PrimerCountry
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.FormField
import io.primer.android.ui.components.TextInputWidget
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.country.SelectCountryFragment
import io.primer.android.viewmodel.PrimerViewModel
import kotlin.properties.Delegates

private const val BOTTOM_MARGIN = 16

internal class FormFieldsFragment : FormChildFragment() {

    private var primerViewModel: PrimerViewModel by Delegates.notNull()

    private var binding: FragmentFormFieldsBinding by autoCleaned()
    private val fieldIds: MutableMap<String, Int> = HashMap()

    interface InputChangeListener {

        fun onValueChange(text: String)
    }

    private sealed class InputElementFactory(protected val field: FormField) {

        abstract fun create(
            ctx: Context,
            l: InputChangeListener,
            initialValue: String,
            onOpenFragment: ((fragment: Fragment) -> Unit)? = null
        ): View

        protected fun createLayoutParams(): LinearLayout.LayoutParams {
            return LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                bottomMargin = BOTTOM_MARGIN
            }
        }

        class EditText(field: FormField) : InputElementFactory(field) {

            override fun create(
                ctx: Context,
                l: InputChangeListener,
                initialValue: String,
                onOpenFragment: ((fragment: Fragment) -> Unit)?
            ): View {

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

        class CountryCodeElement(field: FormField) : InputElementFactory(field) {

            @SuppressLint("ClickableViewAccessibility")
            override fun create(
                ctx: Context,
                l: InputChangeListener,
                initialValue: String,
                onOpenFragment: ((fragment: Fragment) -> Unit)?
            ): View {
                val id = View.generateViewId()
                val inputLayout = View.inflate(ctx, R.layout.country_code_input, null)
                val countryChooserInput = inputLayout.findViewById<MaterialAutoCompleteTextView>(
                    R.id.card_form_country_code_input
                )
                val countryChooserBox = inputLayout.findViewById<TextInputWidget>(
                    R.id.card_form_country_code
                )

                inputLayout.id = id
                countryChooserBox.placeholderText = ctx.getString(field.labelId)
                countryChooserBox.boxBackgroundColor = Color.GRAY

                countryChooserInput.setRawInputType(InputType.TYPE_NULL)
                countryChooserInput.showSoftInputOnFocus = false
                countryChooserInput.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus && (v is android.widget.EditText) && v.text.isNullOrBlank()) {
                        openCountryChooser(onOpenFragment) { country: PrimerCountry ->
                            countryChooserInput.setText(country.name)
                            l.onValueChange(country.code.name)
                        }
                    }
                }
                countryChooserInput.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_UP) v.requestFocus()
                    true
                }

                inputLayout.layoutParams = createLayoutParams()

                return inputLayout
            }

            private fun openCountryChooser(
                onOpenFragment: ((fragment: Fragment) -> Unit)?,
                onSelectCountry: (PrimerCountry) -> Unit
            ) {
                onOpenFragment?.invoke(SelectCountryFragment.newInstance(onSelectCountry))
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

        primerViewModel = ViewModelProvider(requireActivity()).get(PrimerViewModel::class.java)
        primerViewModel.selectCountryCode.observe(viewLifecycleOwner) { country ->
            country ?: return@observe
            parentFragmentManager.commit {
                parentFragmentManager.findFragmentByTag(FRAGMENT_SELECT_COUNTRY_TAG)?.let {
                    remove(it)
                }
            }
        }

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

                        inputField.error = if (value == null) null else value.errorFormatId?.let {
                            requireContext().getString(
                                value.errorFormatId,
                                requireContext().getString(value.fieldId)
                            )
                        } ?: value.errorId?.let { requireContext().getString(it) }
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
            viewModel.getValue(field.name),
            onOpenFragment = { fragment ->
                parentFragmentManager.commit {
                    add(R.id.fragment_country_chooser, fragment, FRAGMENT_SELECT_COUNTRY_TAG)
                }
            }
        )

        fieldIds[field.name] = element.id

        return element
    }

    private fun getElementFactory(field: FormField): InputElementFactory {
        return when (field.inputType) {
            FormField.Type.COUNTRY_CODE -> InputElementFactory.CountryCodeElement(field)
            else -> InputElementFactory.EditText(field)
        }
    }

    private companion object {
        private const val FRAGMENT_SELECT_COUNTRY_TAG = "SelectCountryFragment"
    }
}
