package io.primer.android.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.primer.android.R
import io.primer.android.logging.Logger

class CheckoutSheetFragment : BottomSheetDialogFragment(),
  CheckoutSheetFragmentPublisher {
  private val log = Logger("checkout-fragment")
  private var listener: CheckoutSheetFragmentListener? = null
  private var cameraViewController: CameraViewController? = null;

  override fun onDestroy() {
    super.onDestroy()
    cameraViewController?.onDestroy()
  }

  override fun onPause() {
    super.onPause()
    cameraViewController?.onPause()
  }

  override fun onResume() {
    super.onResume()
    cameraViewController?.onResume()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    cameraViewController?.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  override fun register(listener: CheckoutSheetFragmentListener) {
    this.listener = listener
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    log("onCreateView")

    val view = inflater.inflate(R.layout.activity_checkout_sheet, container, false)

    cameraViewController = CameraViewController(activity, view)

    val button = view?.findViewById<AppCompatTextView>(R.id.scan_card_button)

    button!!.setOnClickListener {
      cameraViewController?.show()
    };

    return view
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    log("onCreate")
    super.onCreate(savedInstanceState)
  }

  override fun onStart() {
    log("onStart")
    super.onStart()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    log("onViewCreated")
    super.onViewCreated(view, savedInstanceState)
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