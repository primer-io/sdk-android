package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.viewmodel.FormViewModel

class FormTitleFragment : Fragment() {
  private val log = Logger("form-title-fragment")
  private lateinit var viewModel: FormViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_form_title, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)

    viewModel.title.observe(viewLifecycleOwner) {
      val titleView = view.findViewById<TextView>(R.id.form_title)
      val descriptionView = view.findViewById<TextView>(R.id.form_title_description)

      it?.let { state ->
        titleView.text = requireContext().getString(state.titleId)

        if (state.descriptionId == null) {
          descriptionView.visibility = View.GONE
        } else {
          descriptionView.visibility = View.VISIBLE
          descriptionView.text = requireContext().getString(state.descriptionId)
        }
      }
    }
  }
}