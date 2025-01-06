package io.primer.android.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.primer.android.R
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.emojiFlag
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.databinding.LayoutBillingAddressFormBinding
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.utils.hideKeyboard

internal class BillingAddressFormView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
    ) : FrameLayout(context, attrs, defStyle), DISdkComponent {
        private val theme: PrimerTheme by
            if (isInEditMode) {
                lazy { PrimerTheme.build() }
            } else {
                inject()
            }

        private val binding =
            LayoutBillingAddressFormBinding.inflate(
                LayoutInflater.from(context),
                this,
                false,
            )

        private val fields = mutableListOf<Pair<PrimerInputElementType, TextInputWidget>>()
        private val fieldsMap by lazy { mutableMapOf<PrimerInputElementType, TextInputWidget>() }

        var onChooseCountry: (() -> Unit)? = null
        var onInputChange: ((PrimerInputElementType, String?) -> Unit)? = null
        var onCountryFocus: ((PrimerInputElementType, Boolean) -> Unit)? = null

        init {
            fields.add(PrimerInputElementType.COUNTRY_CODE to binding.cardFormCountryCode)
            fields.add(PrimerInputElementType.FIRST_NAME to binding.cardFormFirstName)
            fields.add(PrimerInputElementType.LAST_NAME to binding.cardFormLastName)
            fields.add(PrimerInputElementType.ADDRESS_LINE_1 to binding.cardFormAddressLine1)
            fields.add(PrimerInputElementType.ADDRESS_LINE_2 to binding.cardFormAddressLine2)
            fields.add(PrimerInputElementType.POSTAL_CODE to binding.cardFormPostalCode)
            fields.add(PrimerInputElementType.CITY to binding.cardFormCity)
            fields.add(PrimerInputElementType.STATE to binding.cardFormRegion)

            fields.forEach {
                it.second.setupEditTextListeners()
                it.second.setupEditTextTheme()
            }

            addView(binding.root)
            setupListeners()
            setupTheme()
        }

        /**
         * Only available fields
         */
        fun fields(): List<TextInputWidget> =
            fields
                .filter { it.second.isVisible && isVisible }
                .map { it.second }
                .toList()

        /**
         * All fields for billing address.
         */
        fun fieldsMap(): Map<PrimerInputElementType, TextInputWidget> {
            val availableFields = fields.filter { it.second.isVisible }
            fieldsMap.clear()
            availableFields.forEach { pair ->
                fieldsMap[pair.first] = pair.second
            }

            return fieldsMap
        }

        private fun setupTheme() {
            fields.forEach { data ->
                val inputFieldView = data.second
                val fontSize = theme.input.text.fontSize.getDimension(context)
                inputFieldView.editText?.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)

                val color = theme.input.text.defaultColor.getColor(context, theme.isDarkMode)
                inputFieldView.editText?.setTextColor(color)
                inputFieldView.setupEditTextTheme()
                inputFieldView.setupEditTextListeners()

                when (theme.inputMode) {
                    PrimerTheme.InputMode.UNDERLINED -> setInputFieldPadding(inputFieldView)
                    PrimerTheme.InputMode.OUTLINED -> Unit
                }
            }

            val textColor = theme.subtitleText.defaultColor.getColor(context, theme.isDarkMode)
            binding.tvTitleBillingAddress.setTextColor(textColor)
            binding.cardFormCountryCode.setEndIconTintList(ColorStateList.valueOf(textColor))
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupListeners() {
            binding.cardFormCountryCode.editText?.setRawInputType(InputType.TYPE_NULL)
            binding.cardFormCountryCode.editText?.showSoftInputOnFocus = false
            binding.cardFormCountryCode.editText?.setOnFocusChangeListener { v, hasFocus ->
                onCountryFocus?.invoke(PrimerInputElementType.COUNTRY_CODE, hasFocus)
                if (hasFocus && (v is EditText) && v.text.isNullOrBlank()) {
                    focusOnCountryChooser()
                }
            }
            binding.cardFormCountryCode.editText?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) focusOnCountryChooser()
                true
            }

            fields.forEach { data ->
                data.second.onValueChanged = { value ->
                    onInputChange?.invoke(data.first, value?.toString())
                }
            }
        }

        private fun focusOnCountryChooser() {
            hideKeyboard()
            FieldFocuser.focus(binding.cardFormCountryCode)
            onChooseCountry?.invoke()
        }

        private fun setInputFieldPadding(view: View) {
            val res = resources
            val horizontalPadding =
                res
                    .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_horizontal)

            view.setPadding(horizontalPadding, 0, horizontalPadding, 0)
        }

        fun onSelectCountry(country: PrimerCountry) {
            val countryName =
                buildString {
                    append(country.code.emojiFlag())
                    append(" ")
                    append(country.name)
                }
            binding.cardFormCountryCode.editText?.setText(countryName)
            onInputChange?.invoke(PrimerInputElementType.COUNTRY_CODE, country.code.name)
        }

        fun findNextFocus() {
            if (!isVisible) {
                return
            }
            fields.firstOrNull {
                it.second.editText?.text.isNullOrBlank() &&
                    it.second.isVisible && it.second.editText?.isFocused == false
            }?.let { pairTypeView ->
                if (pairTypeView.first != PrimerInputElementType.COUNTRY_CODE) {
                    FieldFocuser.focus(pairTypeView.second)
                } else {
                    hideKeyboard()
                }
            }
        }

        fun onHandleAvailable(billingFields: Map<String, Boolean>?) {
            if (billingFields == null || billingFields.isEmpty()) {
                fields().forEach { it.onFocusChangeListener = null }
                isVisible = false
            } else {
                isVisible = billingFields.values.contains(true)
                fields.forEach { data ->
                    data.second.isVisible = billingFields[data.first.field] ?: false
                }
            }
        }
    }
