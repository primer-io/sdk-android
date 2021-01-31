package io.primer.android.payment.gocardless

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import io.primer.android.PaymentMethod
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.model.Observable
import io.primer.android.model.dto.APIError
import io.primer.android.ui.ButtonState
import io.primer.android.ui.FormErrorState
import io.primer.android.ui.fragments.FormActionEvent
import io.primer.android.ui.fragments.FormActionListener
import io.primer.android.ui.fragments.FormFragment
import io.primer.android.viewmodel.FormViewModel
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import org.json.JSONObject

internal const val DD_FIELD_NAME_IBAN = "iban"
internal const val DD_FIELD_NAME_CUSTOMER_EMAIL = "customerEmail"
internal const val DD_FIELD_NAME_CUSTOMER_NAME = "customerName"
internal const val DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_1 = "customerAddressLine1"
internal const val DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_2 = "customerAddressLine2"
internal const val DD_FIELD_NAME_CUSTOMER_ADDRESS_CITY = "customerAddressCity"
internal const val DD_FIELD_NAME_CUSTOMER_ADDRESS_STATE = "customerAddressState"
internal const val DD_FIELD_NAME_CUSTOMER_ADDRESS_COUNTRY_CODE = "customerAddressCountryCode"
internal const val DD_FIELD_NAME_CUSTOMER_ADDRESS_POSTAL_CODE = "customerAddressPostalCode"

// FR1420041010050500013M02606

class GoCardlessViewFragment : FormFragment() {
  private val log = Logger("gc-form-view")
  private lateinit var viewModel: FormViewModel
  private lateinit var primerViewModel: PrimerViewModel
  private lateinit var tokenizationViewModel: TokenizationViewModel

  private val options: PaymentMethod.GoCardless
    get() = (primerViewModel.selectedPaymentMethod.value as GoCardless).options


  private val firstPageListener = object : FormActionListener {
    override fun onFormAction(e: FormActionEvent) {
      if (e is FormActionEvent.SubmitPressed) {
        showSummaryView()
      }
    }
  }

  private val backToPreviousListener = object : FormActionListener {
    override fun onFormAction(e: FormActionEvent) {
      if (e is FormActionEvent.SubmitPressed) {
        backToPreviousView()
      }
    }
  }

  private val submitFormListener = object : FormActionListener {
    override fun onFormAction(e: FormActionEvent) {
      when (e) {
        is FormActionEvent.SubmitPressed -> onSubmitPressed(e)
        is FormActionEvent.SummaryItemPress -> onSummaryItemPress(e)
      }
    }

    private fun onSubmitPressed(e: FormActionEvent.SubmitPressed) {
      beginTokenization()
    }

    private fun onSummaryItemPress(e: FormActionEvent.SummaryItemPress) {
      when (e.name) {
        "bank" -> showIBANView()
        "customer" -> showCustomerView()
        "address" -> showAddressView()
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)
    primerViewModel = PrimerViewModel.getInstance(requireActivity())
    tokenizationViewModel = TokenizationViewModel.getInstance(requireActivity())

    viewModel.setState(
      IBANViewState(
        mapOf(
          DD_FIELD_NAME_IBAN to "",
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
    )
    setOnFormActionListener(firstPageListener)
  }

  private fun showIBANView() {
    showFormScene(
      IBANViewState(),
      backToPreviousListener
    )
  }

  private fun showSummaryView() {
    showFormScene(
      SummaryViewState(
        getCustomerName = { viewModel.getValue(DD_FIELD_NAME_CUSTOMER_NAME) },
        getCustomerAddress = { formatCustomerAddress() },
        companyAddress = options.companyAddress,
        getBankDetails = { viewModel.getValue(DD_FIELD_NAME_IBAN) },
        legalText = requireContext().getString(R.string.dd_mandate_legal, options.companyName)
      ),
      submitFormListener,
    )
  }

  private fun showCustomerView() {
    showFormScene(
      CustomerDetailsViewState(),
      backToPreviousListener
    )
  }

  private fun showAddressView() {
    showFormScene(
      CustomerAddressViewState(),
      backToPreviousListener,
    )
  }

  private fun backToPreviousView() {
    parentFragmentManager.popBackStack()
  }

  private fun showFormScene(state: GoCardlessFormSceneState, actionListener: FormActionListener) {
    val nextFragment = FormFragment(state)

    nextFragment.setOnFormActionListener(actionListener)

    parentFragmentManager.beginTransaction()
      .setReorderingAllowed(true)
      .addToBackStack(null)
      .replace(R.id.checkout_sheet_content, nextFragment)
      .commit()
  }

  private fun formatCustomerAddress(): String {
    val line1 = viewModel.getValue(DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_1)
    val city = viewModel.getValue(DD_FIELD_NAME_CUSTOMER_ADDRESS_CITY)
    val postalCode = viewModel.getValue(DD_FIELD_NAME_CUSTOMER_ADDRESS_POSTAL_CODE)

    return "$line1, $city, $postalCode"
  }

  private fun beginTokenization() {
    val bankDetails = formatBankDetails()
    val customerDetails = formatCustomerDetails()

    viewModel.setLoading(true)

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
      putIfNotEmpty(this, "email", DD_FIELD_NAME_CUSTOMER_EMAIL)
      putIfNotEmpty(this, "addressLine1", DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_1)
      putIfNotEmpty(this, "addressLine2", DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_2)
      putIfNotEmpty(this, "city", DD_FIELD_NAME_CUSTOMER_ADDRESS_CITY)
      putIfNotEmpty(this, "state", DD_FIELD_NAME_CUSTOMER_ADDRESS_STATE)
      putIfNotEmpty(this, "countryCode", DD_FIELD_NAME_CUSTOMER_ADDRESS_COUNTRY_CODE)
      putIfNotEmpty(this, "postalCode", DD_FIELD_NAME_CUSTOMER_ADDRESS_POSTAL_CODE)

      val name = viewModel.getValue(DD_FIELD_NAME_CUSTOMER_NAME)
      val tokens = name.trim().split(Regex("\\s+"))

      val firstName = if (tokens.size == 1) tokens.first() else tokens.subList(0, tokens.lastIndex)
        .joinToString(" ")
      val lastName = if (tokens.size == 1) "" else tokens.last()

      put("firstName", firstName)
      put("lastName", lastName)
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
    viewModel.setLoading(false)
    viewModel.error.value = FormErrorState(labelId = R.string.dd_mandate_error)
  }

  companion object {
    fun newInstance() = GoCardlessViewFragment()
  }
}