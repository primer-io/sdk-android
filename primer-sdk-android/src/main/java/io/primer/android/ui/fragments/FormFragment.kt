package io.primer.android.ui.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.primer.android.PaymentMethod
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.model.Observable
import io.primer.android.model.dto.APIError
import io.primer.android.payment.gocardless.GoCardless
import io.primer.android.ui.FormViewState
import io.primer.android.viewmodel.*
import org.json.JSONObject
import java.util.*

enum class FormActionType {
  SUBMIT_PRESS,
  SUMMARY_ITEM_PRESS
}

sealed class FormActionEvent(val type: FormActionType) {
  class SubmitPressed : FormActionEvent(FormActionType.SUBMIT_PRESS)
  class SummaryItemPress(val name: String) : FormActionEvent(FormActionType.SUMMARY_ITEM_PRESS)
}

interface FormActionListenerOwner {
  fun getFormActionListener(): FormActionListener?
}

interface FormActionListener {
  fun onFormAction(e: FormActionEvent)
}

open class FormFragment(private val state: FormViewState? = null) : Fragment(), FormActionListenerOwner {
  private val log = Logger("form-fragment")

  private lateinit var viewModel: FormViewModel
  private var mFormActionListener: FormActionListener? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return inflater.inflate(R.layout.fragment_form_view, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)
    state?.let { viewModel.setState(it) }
  }

  override fun onResume() {
    super.onResume()
    log("RESUMED!")
  }

  override fun onPause() {
    super.onPause()
    log("PAUSED!")
  }

  fun setOnFormActionListener(l: FormActionListener?) {
    mFormActionListener = l
  }

  override fun getFormActionListener(): FormActionListener? {
    return mFormActionListener
  }
}