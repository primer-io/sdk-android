package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import io.primer.android.R
import io.primer.android.logging.Logger

internal class FormTitleFragment : FormChildFragment() {
  private val log = Logger("form-title-fragment")

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_form_title, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    view.findViewById<ImageView>(R.id.form_title_go_back).setOnClickListener {
      dispatchFormEvent(FormActionEvent.GoBack())
    }

    view.findViewById<Button>(R.id.form_title_cancel_button).setOnClickListener {
      dispatchFormEvent(FormActionEvent.Cancel())
    }

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