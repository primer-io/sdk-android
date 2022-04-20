package io.primer.android.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.databinding.LayoutBillingAddressFormBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.Country
import io.primer.android.model.dto.CountryCode
import io.primer.android.model.dto.PrimerInputFieldType
import io.primer.android.model.dto.emojiFlag
import io.primer.android.ui.FieldFocuser
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@OptIn(KoinApiExtension::class)
internal class BillingAddressFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), DIAppComponent {

    private val theme: PrimerTheme by inject()

    private val binding = LayoutBillingAddressFormBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )

    private val fields = mutableListOf<Pair<PrimerInputFieldType, TextInputWidget>>()
    private val fieldsMap by lazy { mutableMapOf<PrimerInputFieldType, TextInputWidget>() }

    var onChooseCountry: (() -> Unit)? = null
    var onHideKeyboard: (() -> Unit)? = null
    var onInputChange: ((PrimerInputFieldType, String?) -> Unit)? = null

    init {
        fields.add(PrimerInputFieldType.COUNTRY_CODE to binding.cardFormCountryCode)
        fields.add(PrimerInputFieldType.FIRST_NAME to binding.cardFormFirstName)
        fields.add(PrimerInputFieldType.LAST_NAME to binding.cardFormLastName)
        fields.add(PrimerInputFieldType.ADDRESS_LINE_1 to binding.cardFormAddressLine1)
        fields.add(PrimerInputFieldType.ADDRESS_LINE_2 to binding.cardFormAddressLine2)
        fields.add(PrimerInputFieldType.POSTAL_CODE to binding.cardFormPostalCode)
        fields.add(PrimerInputFieldType.CITY to binding.cardFormCity)
        fields.add(PrimerInputFieldType.STATE to binding.cardFormRegion)

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
    fun fields(): List<TextInputWidget> = fields
        .filter { it.second.isVisible }
        .map { it.second }
        .toList()

    /**
     * All fields for billing address.
     */
    fun fieldsMap(): Map<PrimerInputFieldType, TextInputWidget> {
        val availableFields = fields
        fieldsMap.clear()
        availableFields.forEach { pair ->
            fieldsMap[pair.first] = pair.second
        }

        return fieldsMap
    }

    private fun setupTheme() {
        fields.forEach { data ->
            val inputFieldView = data.second
            val fontSize = theme.input.text.fontsize.getDimension(context)
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
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        binding.cardFormCountryCode.editText?.setRawInputType(InputType.TYPE_NULL)
        binding.cardFormCountryCode.editText?.showSoftInputOnFocus = false
        binding.cardFormCountryCode.editText?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && (v is EditText) && v.text.isNullOrBlank()) {
                onHideKeyboard?.invoke()
                onChooseCountry?.invoke()
            } else {
                focusOnFirstName()
            }
        }
        binding.cardFormCountryCode.editText?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) onChooseCountry?.invoke()
            true
        }

        fields.forEach { data ->
            data.second.onValueChanged = { value ->
                onInputChange?.invoke(data.first, value?.toString())
            }
        }
    }

    private fun focusOnFirstName() {
        FieldFocuser.focus(binding.cardFormFirstName)
    }

    private fun setInputFieldPadding(view: View) {
        val res = resources
        val horizontalPadding = res
            .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_horizontal)

        view.setPadding(horizontalPadding, 0, horizontalPadding, 0)
    }

    fun onLoadCountry(countryCode: CountryCode?) {
        binding.cardFormPostalCode.hint = when (countryCode) {
            CountryCode.US -> resources.getString(R.string.card_zip)
            else -> resources.getString(R.string.address_postal_code)
        }
    }

    fun onSelectCountry(country: Country) {
        val countryName = buildString {
            append(country.code.emojiFlag())
            append(" ")
            append(country.name)
        }
        binding.cardFormCountryCode.editText?.setText(countryName)
        onInputChange?.invoke(PrimerInputFieldType.COUNTRY_CODE, country.code.name)
        findNextFocus()
    }

    fun findNextFocus() {
        fields.firstOrNull {
            it.second.editText?.text.isNullOrBlank() &&
                it.second.isVisible
        }?.let { FieldFocuser.focus(it.second) }
    }

    fun onHandleAvailable(billingFields: Map<String, Boolean>?) {
        if (billingFields == null) {
            isVisible = false
        } else {
            fields.forEach { data ->
                data.second.isVisible = billingFields[data.first.field] ?: false
            }
        }
    }
}
