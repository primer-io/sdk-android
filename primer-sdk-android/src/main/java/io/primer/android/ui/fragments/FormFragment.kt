package io.primer.android.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
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
open class FormFragment(
    private val state: FormViewState? = null, // FIXME this should not be passed via ctor
) : Fragment(), FormActionListenerOwner {

    private lateinit var viewModel: FormViewModel
    private lateinit var tokenizationViewModel: TokenizationViewModel
    private lateinit var primerViewModel: PrimerViewModel
    private var formActionListener: FormActionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_form_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("RUI", "> FormFragment onViewCreated")

        viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)
        tokenizationViewModel = TokenizationViewModel.getInstance(requireActivity())
        primerViewModel = PrimerViewModel.getInstance(requireActivity())

        tokenizationViewModel.tokenizationData.observe(viewLifecycleOwner) {
            Log.d("RUI", "FormFragment observed tokenizationData")
            onTokenizeSuccess()
        }

        tokenizationViewModel.tokenizationError.observe(viewLifecycleOwner) {
            it?.let { onTokenizeError() }
        }

        state?.let { viewModel.setState(it) }
    }

    private val goCardlessMandateObserver: Observer<JSONObject> = Observer<JSONObject> { data ->
        Log.d("RUI", "observed goCardlessMandate > $data")
        onMandateCreated(data)
    }

    private val goCardlessMandateErrorObserver: Observer<Unit> = Observer<Unit> {
        Log.d("RUI", "observed goCardlessMandateError")
        onTokenizeError()
    }

    override fun onStart() {
        super.onStart()
        tokenizationViewModel.goCardlessMandate.observeForever(goCardlessMandateObserver)
        tokenizationViewModel.goCardlessMandateError.observeForever(goCardlessMandateErrorObserver)
    }

    override fun onStop() {
        super.onStop()
        tokenizationViewModel.goCardlessMandate.removeObserver(goCardlessMandateObserver)
        tokenizationViewModel.goCardlessMandateError.removeObserver(goCardlessMandateErrorObserver)
        Log.d("RUI", "< FormFragment onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("RUI", "< FormFragment onDestroyView")
    }

    private fun onMandateCreated(data: JSONObject) {
        Log.d("RUI", "FormFragment onMandateCreated()")

        val mandateId = data.getString("mandateId")
        tokenizationViewModel.resetPaymentMethod(primerViewModel.selectedPaymentMethod.value)
        tokenizationViewModel.setTokenizableValue("gocardlessMandateId", mandateId)
        tokenizationViewModel.tokenize()
    }

    private fun onTokenizeSuccess() {
        // ??
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
