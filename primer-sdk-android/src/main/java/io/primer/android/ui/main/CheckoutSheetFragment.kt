package io.primer.android.ui.main

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.primer.android.R
import io.primer.android.logging.Logger
//import kotlinx.android.synthetic.main.bottom_sheet_options.*
import java.lang.ref.WeakReference

class CheckoutSheetFragment : BottomSheetDialogFragment(), CheckoutSheetFragmentPublisher {
  private val log = Logger("checkout-fragment")
  private var listener: CheckoutSheetFragmentListener? = null

  override fun register(listener: CheckoutSheetFragmentListener) {
    this.listener = listener
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.activity_checkout_sheet, container, false)
  }

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    this.listener?.onDismissed()
  }

  companion object {
    @JvmStatic
    fun newInstance(bundle: Bundle): CheckoutSheetFragment {
      val fragment = CheckoutSheetFragment()
      fragment.arguments = bundle
      return fragment
    }
  }
}