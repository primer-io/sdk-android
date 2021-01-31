package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.primer.android.R
import io.primer.android.viewmodel.FormViewModel

internal class FormErrorFragment : FormChildFragment() {
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_form_errors, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.error.observe(viewLifecycleOwner) {
      view.visibility = if (it == null) View.GONE else View.VISIBLE

      it?.let { state ->
        view.findViewById<TextView>(R.id.form_error_text).text = requireContext().getText(state.labelId)
      }
    }
  }
}