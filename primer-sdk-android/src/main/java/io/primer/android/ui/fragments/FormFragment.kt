package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.primer.android.R
import io.primer.android.ui.FormErrorState
import io.primer.android.ui.FormViewState
import io.primer.android.viewmodel.FormViewModel
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import org.json.JSONObject

enum class FormActionType {
    SUBMIT_PRESS,
    SUMMARY_ITEM_PRESS,
    GO_BACK,
    CANCEL,
    EXIT,
}

sealed class FormActionEvent(val type: FormActionType) {
    class SubmitPressed : FormActionEvent(FormActionType.SUBMIT_PRESS)
    class SummaryItemPress(val name: String) : FormActionEvent(FormActionType.SUMMARY_ITEM_PRESS)
    class GoBack : FormActionEvent(FormActionType.GO_BACK)
    class Cancel : FormActionEvent(FormActionType.CANCEL)
    class Exit : FormActionEvent(FormActionType.EXIT)
}

interface FormActionListenerOwner {

    fun getFormActionListener(): FormActionListener?
}

interface FormActionListener {

    fun onFormAction(e: FormActionEvent)
}

// FIXME drop inheritance
// FIXME move to gocardless as it's only used there
open class FormFragment(
    private val state: FormViewState? = null, // FIXME this should not be passed via ctor
) : Fragment(), FormActionListenerOwner {

    private lateinit var viewModel: FormViewModel
    private lateinit var tokenizationViewModel: TokenizationViewModel
    private lateinit var primerViewModel: PrimerViewModel
    private var formActionListener: FormActionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        inflater.inflate(R.layout.fragment_form_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)
        tokenizationViewModel = TokenizationViewModel.getInstance(requireActivity())
        primerViewModel = PrimerViewModel.getInstance(requireActivity())

        tokenizationViewModel.tokenizationData.observe(viewLifecycleOwner) {
            onTokenizeSuccess()
        }

        tokenizationViewModel.tokenizationError.observe(viewLifecycleOwner) {
            it?.let { onTokenizeError() }
        }

        state?.let { viewModel.setState(it) }
    }

    private val goCardlessMandateObserver: Observer<JSONObject> = Observer<JSONObject> { data ->
        onMandateCreated(data)
    }

    private val goCardlessMandateErrorObserver: Observer<Unit> = Observer<Unit> {
        onTokenizeError()
    }

    // FIXME unfortunately we need this gocardless mandate logic to be here and it needs to be
    //  observed/ in onStart/Stop because of the way the fragment are displayed in succession
    //  (GoCardlessViewFragment issues requests but then it displays another FormFragment on top)
    override fun onStart() {
        super.onStart()
        tokenizationViewModel.goCardlessMandate.observeForever(goCardlessMandateObserver)
        tokenizationViewModel.goCardlessMandateError.observeForever(goCardlessMandateErrorObserver)
    }

    override fun onStop() {
        super.onStop()
        tokenizationViewModel.goCardlessMandate.removeObserver(goCardlessMandateObserver)
        tokenizationViewModel.goCardlessMandateError.removeObserver(goCardlessMandateErrorObserver)
    }

    private fun onMandateCreated(data: JSONObject) {
        // FIXME we shouldn't be parsing json here
        val mandateId = data.getString("mandateId")
        tokenizationViewModel.resetPaymentMethod(primerViewModel.selectedPaymentMethod.value)
        tokenizationViewModel.setTokenizableValue("gocardlessMandateId", mandateId)
        tokenizationViewModel.tokenize()
    }

    private fun onTokenizeSuccess() {
        // FIXME why do we need this?
    }

    private fun onTokenizeError() {
        viewModel.setLoading(false)
        viewModel.error.value = FormErrorState(labelId = R.string.dd_mandate_error)
    }

    fun setOnFormActionListener(listener: FormActionListener?) {
        formActionListener = listener
    }

    override fun getFormActionListener(): FormActionListener? {
        return formActionListener
    }
}
