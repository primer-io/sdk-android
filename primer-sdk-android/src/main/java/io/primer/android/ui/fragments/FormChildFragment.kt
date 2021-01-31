package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.primer.android.viewmodel.FormViewModel

internal open class FormChildFragment : Fragment() {
  protected lateinit var viewModel: FormViewModel

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)
  }

  protected fun dispatchFormEvent(e: FormActionEvent) {
    parentFragment?.takeIf { it is FormActionListenerOwner }?.also {
      (it as FormActionListenerOwner).getFormActionListener()?.onFormAction(e)
    }
  }
}