package io.primer.android.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.model.Model
import io.primer.android.viewmodel.PrimerViewModel

internal class CheckoutSheetFragment : BottomSheetDialogFragment() {
  private val log = Logger("checkout-fragment")
  private lateinit var viewModel: PrimerViewModel

  @SuppressLint("RestrictedApi")
  override fun setupDialog(dialog: Dialog, style: Int) {
    super.setupDialog(dialog, style)

    dialog.setCanceledOnTouchOutside(false)
    (dialog as BottomSheetDialog).behavior.isHideable = false
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.Primer_BottomSheet)

    activity?.let {
      viewModel = ViewModelProviders.of(it).get(PrimerViewModel::class.java)
    }

//    viewModel = PrimerViewModel.getInstance(requireActivity())
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_bottom_sheet, container, false)
  }

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    viewModel.setSheetDismissed(true)
  }

  private fun <T: View> findViewById(id: Int): T {
    return requireView().findViewById(id)
  }

  companion object {
    @JvmStatic
    fun newInstance() = CheckoutSheetFragment()
  }
}