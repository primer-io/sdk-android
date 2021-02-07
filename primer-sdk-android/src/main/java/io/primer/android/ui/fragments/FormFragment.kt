package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.ui.FormViewState
import io.primer.android.viewmodel.FormViewModel

enum class FormActionType {
  SUBMIT_PRESS,
  SUMMARY_ITEM_PRESS,
  GO_BACK,
  CANCEL,
}

sealed class FormActionEvent(val type: FormActionType) {
  class SubmitPressed : FormActionEvent(FormActionType.SUBMIT_PRESS)
  class SummaryItemPress(val name: String) : FormActionEvent(FormActionType.SUMMARY_ITEM_PRESS)
  class GoBack : FormActionEvent(FormActionType.GO_BACK)
  class Cancel : FormActionEvent(FormActionType.CANCEL)
}

interface FormActionListenerOwner {
  fun getFormActionListener(): FormActionListener?
}

interface FormActionListener {
  fun onFormAction(e: FormActionEvent)
}

open class FormFragment(private val state: FormViewState? = null) : Fragment(),
  FormActionListenerOwner {
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

  fun setOnFormActionListener(l: FormActionListener?) {
    mFormActionListener = l
  }

  override fun getFormActionListener(): FormActionListener? {
    return mFormActionListener
  }
}