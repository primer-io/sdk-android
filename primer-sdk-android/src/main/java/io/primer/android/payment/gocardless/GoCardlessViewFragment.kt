package io.primer.android.payment.gocardless

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import io.primer.android.PaymentMethod
import io.primer.android.R
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.logging.Logger
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.ui.FormErrorState
import io.primer.android.ui.FormTitleState
import io.primer.android.ui.fragments.FormActionEvent
import io.primer.android.ui.fragments.FormActionListener
import io.primer.android.ui.fragments.FormFragment
import io.primer.android.viewmodel.FormViewModel
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.ViewStatus
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension

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

@KoinApiExtension
class GoCardlessViewFragment : FormFragment() {

    private val log = Logger("gc-form-view")
    private lateinit var viewModel: FormViewModel
    private lateinit var primerViewModel: PrimerViewModel
    private lateinit var tokenizationViewModel: TokenizationViewModel
    private var readyToTokenize = false

    private val options: PaymentMethod.GoCardless
        get() = (primerViewModel.selectedPaymentMethod.value as GoCardless).options

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)
        primerViewModel = PrimerViewModel.getInstance(requireActivity())
        tokenizationViewModel = TokenizationViewModel.getInstance(requireActivity())

        showFormScene(
            IBANViewState(
                buttonLabelId = R.string.next,
                cancelBehaviour = FormTitleState.CancelBehaviour.EXIT,
                showProgress = true,
                mapOf(
                    DD_FIELD_NAME_IBAN to "",
                    DD_FIELD_NAME_CUSTOMER_EMAIL to (options.customerEmail ?: ""),
                    DD_FIELD_NAME_CUSTOMER_NAME to (options.customerName ?: ""),
                    DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_1 to (options.customerAddressLine1 ?: ""),
                    DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_2 to (options.customerAddressLine2 ?: ""),
                    DD_FIELD_NAME_CUSTOMER_ADDRESS_CITY to (options.customerAddressCity ?: ""),
                    DD_FIELD_NAME_CUSTOMER_ADDRESS_STATE to (options.customerAddressState ?: ""),
                    DD_FIELD_NAME_CUSTOMER_ADDRESS_COUNTRY_CODE to (options.customerAddressCountryCode ?: ""),
                    DD_FIELD_NAME_CUSTOMER_ADDRESS_POSTAL_CODE to (options.customerAddressPostalCode ?: ""),
                )
            ),
            isTransition = false
        )
    }

    private fun onSummaryItemPress(e: FormActionEvent.SummaryItemPress) {
        when (e.name) {
            "bank" -> showIBANView(R.string.confirm)
            "customer-name" -> showCustomerNameView(R.string.confirm)
            "customer-email" -> showCustomerEmailView(R.string.confirm)
            "address" -> showAddressView(R.string.confirm)
        }
    }

    private fun createFormActionListener(scene: GoCardlessFormSceneState.Scene): FormActionListener {
        return object : FormActionListener {
            override fun onFormAction(e: FormActionEvent) {
                when (e) {
                    is FormActionEvent.Cancel -> onCancel()
                    is FormActionEvent.Exit -> onExit()
                    is FormActionEvent.GoBack -> backToPreviousView()
                    is FormActionEvent.SummaryItemPress -> onSummaryItemPress(e)
                    is FormActionEvent.SubmitPressed -> onSubmitPressed(scene)
                }
            }
        }
    }

    private fun onSubmitPressed(scene: GoCardlessFormSceneState.Scene) {
        if (readyToTokenize) {
            return if (scene == GoCardlessFormSceneState.Scene.SUMMARY) beginTokenization() else backToPreviousView()
        }

        val hasName = viewModel.getValue(DD_FIELD_NAME_CUSTOMER_NAME).isNotEmpty()
        val hasEmail = viewModel.getValue(DD_FIELD_NAME_CUSTOMER_EMAIL).isNotEmpty()
        val hasAddress =
            viewModel.getValue(DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_1).isNotEmpty() &&
                viewModel.getValue(DD_FIELD_NAME_CUSTOMER_ADDRESS_CITY).isNotEmpty() &&
                viewModel.getValue(DD_FIELD_NAME_CUSTOMER_ADDRESS_POSTAL_CODE).isNotEmpty() &&
                viewModel.getValue(DD_FIELD_NAME_CUSTOMER_ADDRESS_COUNTRY_CODE).isNotEmpty()

        var nextScene = scene

        if (nextScene == GoCardlessFormSceneState.Scene.IBAN) {
            nextScene = GoCardlessFormSceneState.Scene.CUSTOMER_NAME
        }

        if (nextScene == GoCardlessFormSceneState.Scene.CUSTOMER_NAME && hasName) {
            nextScene = GoCardlessFormSceneState.Scene.CUSTOMER_EMAIL
        }

        if (nextScene == GoCardlessFormSceneState.Scene.CUSTOMER_EMAIL && hasEmail) {
            nextScene = GoCardlessFormSceneState.Scene.ADDRESS
        }

        if (nextScene == GoCardlessFormSceneState.Scene.ADDRESS && hasAddress) {
            nextScene = GoCardlessFormSceneState.Scene.SUMMARY
        }

        if (nextScene == GoCardlessFormSceneState.Scene.SUMMARY) {
            readyToTokenize = true
        }

        when (nextScene) {
            GoCardlessFormSceneState.Scene.CUSTOMER_NAME -> showCustomerNameView(R.string.next)
            GoCardlessFormSceneState.Scene.CUSTOMER_EMAIL -> showCustomerEmailView(R.string.next)
            GoCardlessFormSceneState.Scene.ADDRESS -> showAddressView(R.string.next)
            GoCardlessFormSceneState.Scene.SUMMARY -> showSummaryView()
            else -> {
            }
        }
    }

    private fun showIBANView(buttonLabelId: Int) {
        showFormScene(IBANViewState(buttonLabelId, cancelBehaviour = FormTitleState.CancelBehaviour.CANCEL, showProgress = false))
    }

    private fun showSummaryView() {
        showFormScene(
            SummaryViewState(
                getCustomerName = { viewModel.getValue(DD_FIELD_NAME_CUSTOMER_NAME) },
                getCustomerEmail = { viewModel.getValue(DD_FIELD_NAME_CUSTOMER_EMAIL) },
                getCustomerAddress = { formatCustomerAddress() },
                companyAddress = options.companyAddress,
                getBankDetails = { viewModel.getValue(DD_FIELD_NAME_IBAN) },
                legalText = requireContext().getString(R.string.dd_mandate_legal, options.companyName)
            ),
        )
    }

    private fun showCustomerEmailView(buttonLabelId: Int) {
        showFormScene(
            CustomerEmailViewState(buttonLabelId, showProgress = !readyToTokenize),
        )
    }

    private fun showCustomerNameView(buttonLabelId: Int) {
        showFormScene(
            CustomerNameViewState(buttonLabelId, showProgress = !readyToTokenize),
        )
    }

    private fun showAddressView(buttonLabelId: Int) {
        showFormScene(
            CustomerAddressViewState(buttonLabelId, showProgress = !readyToTokenize),
        )
    }

    private fun backToPreviousView() {
        parentFragmentManager.popBackStack()
    }

    private fun onCancel() {
        AlertDialog.Builder(context).apply {
            setTitle(R.string.dd_cancel_message)
            setMessage(R.string.data_will_be_lost)
            setPositiveButton(R.string.confirm) { _, _ ->
                onConfirmCancel()
            }
            setNegativeButton(R.string.cancel) { _, _ -> }
            show()
        }
    }

    private fun onExit() {
        EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.DISMISSED_BY_USER))
    }

    private fun onConfirmCancel() {
        primerViewModel.viewStatus.value = ViewStatus.SELECT_PAYMENT_METHOD
    }

    private fun showFormScene(state: GoCardlessFormSceneState, isTransition: Boolean = true) {
        val fragment = if (isTransition) FormFragment(state) else this
        val listener = createFormActionListener(state.scene)

        fragment.setOnFormActionListener(listener)

        if (isTransition) {
            parentFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .replace(R.id.checkout_sheet_content, fragment)
                .commit()
        } else {
            viewModel.setState(state)
        }
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
        viewModel.error.value = null

        primerViewModel.selectedPaymentMethod.value?.config?.id?.let { id ->
            tokenizationViewModel.createGoCardlessMandate(id, bankDetails, customerDetails)
            tokenizationViewModel.goCardlessMandate.observe(this) { data ->
                onMandateCreated(data)
            }
            tokenizationViewModel.goCardlessMandateError.observe(this) {
                onTokenizeError()
            }
//            tokenizationViewModel.createGoCardlessMandate(id, bankDetails, customerDetails).observe {
//                when (it) {
//                    is Observable.ObservableSuccessEvent -> onMandateCreated(it.data)
//                    is Observable.ObservableErrorEvent -> onTokenizeError()
//                }
//            }
        }
    }

    private fun formatBankDetails(): JSONObject = JSONObject().apply {
        put("iban", viewModel.getValue(DD_FIELD_NAME_IBAN))
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
        tokenizationViewModel.resetPaymentMethod(primerViewModel.selectedPaymentMethod.value)
        tokenizationViewModel.setTokenizableValue("gocardlessMandateId", mandateId)

        tokenizationViewModel.tokenizationData.observe(viewLifecycleOwner) {
            onTokenizeSuccess()
        }

        tokenizationViewModel.tokenizationError.observe(viewLifecycleOwner) {
            it?.let { onTokenizeError() }
        }
    }

    private fun onTokenizeSuccess() {}

    private fun onTokenizeError() {
        viewModel.setLoading(false)
        viewModel.error.value = FormErrorState(labelId = R.string.dd_mandate_error)
    }

    companion object {

        fun newInstance(): GoCardlessViewFragment = GoCardlessViewFragment()
    }
}
