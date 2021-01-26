package io.primer.android.ui.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import io.primer.android.PaymentMethod
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.model.Observable
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.gocardless.GoCardless
import io.primer.android.viewmodel.*
import io.primer.android.viewmodel.FormViewModel
import org.json.JSONObject
import java.util.*

internal const val DD_FIELD_NAME_IBAN = "iban"
internal const val DD_FIELD_NAME_CUSTOMER_EMAIL = "customerEmail"
internal const val DD_FIELD_NAME_CUSTOMER_NAME = "customerName"
internal const val DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_1 = "customerAddressLine1"
internal const val DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_2 = "customerAddressLine2"
internal const val DD_FIELD_NAME_CUSTOMER_ADDRESS_CITY = "customerAddressCity"
internal const val DD_FIELD_NAME_CUSTOMER_ADDRESS_STATE = "customerAddressState"
internal const val DD_FIELD_NAME_CUSTOMER_ADDRESS_COUNTRY_CODE = "customerAddressCountryCode"
internal const val DD_FIELD_NAME_CUSTOMER_ADDRESS_POSTAL_CODE = "customerAddressPostalCode"

class GoCardlessFragment : Fragment() {
  private val log = Logger("go-cardless-fragment")
  private lateinit var viewModel : FormViewModel
  private lateinit var primerViewModel : PrimerViewModel
  private lateinit var tokenizationViewModel: TokenizationViewModel

  private val options: PaymentMethod.GoCardless
      get() = (primerViewModel.selectedPaymentMethod.value as GoCardless).options

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    primerViewModel = PrimerViewModel.getInstance(requireActivity())
    tokenizationViewModel = TokenizationViewModel.getInstance(requireActivity())

    viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)
    viewModel.setInitialValues(
      mapOf(
        DD_FIELD_NAME_IBAN to "FR1420041010050500013M02606", // TODO: remove this
        DD_FIELD_NAME_CUSTOMER_EMAIL to options.customerEmail,
        DD_FIELD_NAME_CUSTOMER_NAME to options.customerName,
        DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_1 to options.customerAddressLine1,
        DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_2 to (options.customerAddressLine2 ?: ""),
        DD_FIELD_NAME_CUSTOMER_ADDRESS_CITY to options.customerAddressCity,
        DD_FIELD_NAME_CUSTOMER_ADDRESS_STATE to (options.customerAddressState ?: ""),
        DD_FIELD_NAME_CUSTOMER_ADDRESS_COUNTRY_CODE to options.customerAddressCountryCode,
        DD_FIELD_NAME_CUSTOMER_ADDRESS_POSTAL_CODE to options.customerAddressPostalCode,
      )
    )

    showIBANView()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return inflater.inflate(R.layout.fragment_direct_debit_form, container, false)
  }

  override fun onDestroy() {
    super.onDestroy()
    viewModel.reset()
  }

  private fun showIBANView() {
    prepareView(
      title = FormTitleState(
        titleId = R.string.add_bank_account,
        descriptionId = R.string.sepa_core_description
      ),
      fields = listOf(
        FormField(
          name = DD_FIELD_NAME_IBAN,
          labelId = R.string.iban,
          inputType = InputType.TYPE_CLASS_TEXT,
          required = true,
          autoFocus = true,
          minLength = 15
        )
      ),
      button = ButtonState(
        labelId = R.string.next,
      )
    )

    viewModel.setOnButtonPressListener(object : FormViewModel.ButtonPressListener {
      override fun onButtonPressed() {
        showSummary()
      }
    })
  }

  private fun showSummary() {
    prepareView(
      title = FormTitleState(
        titleId = R.string.confirm_dd_mandate,
      ),
      button = ButtonState(
        labelId = R.string.confirm,
      ),
      summary = FormSummaryState(
        items = listOf(
          InteractiveSummaryItem(
            name = "customer",
            iconId = R.drawable.icon_user,
            label = viewModel.getValue(DD_FIELD_NAME_CUSTOMER_NAME)
          ),
          InteractiveSummaryItem(
            name = "address",
            iconId = R.drawable.icon_location_pin,
            label = formatCustomerAddress(),
          ),
          InteractiveSummaryItem(
            name = "bank",
            iconId = R.drawable.icon_bank,
            label = viewModel.getValue(DD_FIELD_NAME_IBAN)
          )
        ),
        text = listOf(
          TextSummaryItem(
            content = options.companyAddress
          ),
          TextSummaryItem(
            content = requireContext().getString(R.string.dd_mandate_legal, options.companyName),
            styleId = R.style.Primer_Text_SmallPrint
          )
        )
      )
    )

    viewModel.setOnSummaryItemPressListener(object : FormViewModel.SummaryItemPressListener {
      override fun onSummaryItemPressed(name: String) {
        when (name) {
          "customer" -> showCustomerDetails()
          "address" -> showAddressDetails()
          "bank" -> showIBANView()
        }
      }
    })

    viewModel.setOnButtonPressListener(object : FormViewModel.ButtonPressListener {
      override fun onButtonPressed() {
        beginTokenization()
      }
    })
  }

  private fun showCustomerDetails() {
    prepareView(
      title = FormTitleState(
        titleId = R.string.confirm_dd_mandate,
      ),
      fields = listOf(
        FormField(
          name = DD_FIELD_NAME_CUSTOMER_NAME,
          labelId = R.string.name,
          required = true,
          autoFocus = true,
          inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
          minWordCount = 2,
        ),
        FormField(
          name = DD_FIELD_NAME_CUSTOMER_EMAIL,
          labelId = R.string.email,
          required = true,
          inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
        ),
      ),
      button = ButtonState(
        labelId = R.string.confirm,
      )
    )

    viewModel.setOnButtonPressListener(object : FormViewModel.ButtonPressListener {
      override fun onButtonPressed() {
        showSummary()
      }
    })
  }

  private fun formatCustomerAddress(): String {
    val line1 = viewModel.getValue(DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_1)
    val city = viewModel.getValue(DD_FIELD_NAME_CUSTOMER_ADDRESS_CITY)
    val postalCode = viewModel.getValue(DD_FIELD_NAME_CUSTOMER_ADDRESS_POSTAL_CODE)

    return "$line1, $city, $postalCode"
  }

  private fun showAddressDetails() {
    prepareView(
      title = FormTitleState(
        titleId = R.string.confirm_dd_mandate,
      ),
      fields = listOf(
        FormField(
          name = DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_1,
          labelId = R.string.address_line_1,
          required = true,
          autoFocus = true,
          inputType = InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS,
        ),
        FormField(
          name = DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_2,
          labelId = R.string.address_line_2,
          required = false,
          inputType = InputType.TYPE_CLASS_TEXT,
        ),
        FormField(
          name = DD_FIELD_NAME_CUSTOMER_ADDRESS_CITY,
          labelId = R.string.address_city,
          required = true,
          inputType = InputType.TYPE_CLASS_TEXT,
        ),
        FormField(
          name = DD_FIELD_NAME_CUSTOMER_ADDRESS_STATE,
          labelId = R.string.address_state,
          required = false,
          inputType = InputType.TYPE_CLASS_TEXT,
        ),
        FormField(
          name = DD_FIELD_NAME_CUSTOMER_ADDRESS_COUNTRY_CODE,
          labelId = R.string.address_country_code,
          required = true,
          inputType = InputType.TYPE_CLASS_TEXT,
          minLength = 2,
        ),
        FormField(
          name = DD_FIELD_NAME_CUSTOMER_ADDRESS_POSTAL_CODE,
          labelId = R.string.address_postal_code,
          required = true,
          inputType = InputType.TYPE_CLASS_TEXT,
        ),
      ),
      button = ButtonState(
        labelId = R.string.confirm,
      )
    )

    viewModel.setOnButtonPressListener(object : FormViewModel.ButtonPressListener {
      override fun onButtonPressed() {
        showSummary()
      }
    })
  }

  private fun prepareView(title: FormTitleState? = null, fields: List<FormField> = Collections.emptyList(), button: ButtonState? = null, summary: FormSummaryState? = null) {
    viewModel.title.value = title
    viewModel.fields.value = fields
    viewModel.summary.value = summary
    viewModel.button.value = button
    viewModel.errorId.value = null
  }

  private fun beginTokenization() {
    val bankDetails = formatBankDetails()
    val customerDetails = formatCustomerDetails()

    viewModel.button.value = ButtonState(
      labelId = R.string.confirm,
      loading = true
    )

    primerViewModel.selectedPaymentMethod.value?.config?.id?.let { id ->
      tokenizationViewModel.createGoCardlessMandate(id, bankDetails, customerDetails).observe {
        when (it) {
          is Observable.ObservableSuccessEvent -> onMandateCreated(it.data)
          is Observable.ObservableErrorEvent -> onTokenizeError(it.error)
        }
      }
    }
  }

  private fun formatBankDetails(): JSONObject {
    return JSONObject().apply {
      put("iban", viewModel.getValue(DD_FIELD_NAME_IBAN))
    }
  }

  private fun formatCustomerDetails(): JSONObject {
    return JSONObject().apply {
      putIfNotEmpty(this,"email", DD_FIELD_NAME_CUSTOMER_EMAIL)
      putIfNotEmpty(this, "addressLine1", DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_1)
      putIfNotEmpty(this, "addressLine2", DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_2)
      putIfNotEmpty(this, "city", DD_FIELD_NAME_CUSTOMER_ADDRESS_CITY)
      putIfNotEmpty(this, "state", DD_FIELD_NAME_CUSTOMER_ADDRESS_STATE)
      putIfNotEmpty(this, "countryCode", DD_FIELD_NAME_CUSTOMER_ADDRESS_COUNTRY_CODE)
      putIfNotEmpty(this, "postalCode", DD_FIELD_NAME_CUSTOMER_ADDRESS_POSTAL_CODE)

      val name = viewModel.getValue(DD_FIELD_NAME_CUSTOMER_NAME)
      val tokens = name.trim().split(Regex("\\s+"))

      val firstName = if (tokens.size == 1) tokens.first() else tokens.subList(0, tokens.lastIndex).joinToString(" ")
      val lastName = if (tokens.size == 1) "" else tokens.last()

      put("firstName", firstName)
      put("lastName", lastName)

      log(this.toString())
    }
  }

  private fun putIfNotEmpty(obj: JSONObject, key: String, valueKey: String) {
    val value = viewModel.getValue(valueKey)

    if (value.isNotEmpty()) {
      obj.put(key, value)
    }
  }

  private fun onMandateCreated(data: JSONObject) {
    val mandateId = data.getString("mandateId")
    tokenizationViewModel.reset(primerViewModel.selectedPaymentMethod.value)
    tokenizationViewModel.setTokenizableValue("gocardlessMandateId", mandateId)

    tokenizationViewModel.tokenize().observe {
      when (it) {
        is Observable.ObservableSuccessEvent -> onTokenizeSuccess()
        is Observable.ObservableErrorEvent -> onTokenizeError(it.error)
      }
    }
  }

  private fun onTokenizeSuccess() {}

  private fun onTokenizeError(error: APIError) {
    viewModel.button.value = ButtonState(
      labelId = R.string.confirm,
      loading = false
    )

    viewModel.errorId.value = R.string.dd_mandate_error
  }

  companion object {
    fun newInstance() = GoCardlessFragment()
  }
}