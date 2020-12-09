package io.primer.android.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.ui.KeyboardVisibilityEvent
import io.primer.android.viewmodel.PrimerViewModel

internal class CheckoutSheetFragment : BottomSheetDialogFragment(),
  KeyboardVisibilityEvent.OnChangedListener {
  private val log = Logger("checkout-fragment")
  private lateinit var viewModel: PrimerViewModel
  private lateinit var keyboardVisibilityEvent: KeyboardVisibilityEvent

  override fun onKeyboardVisibilityChanged(visible: Boolean) {
    viewModel.keyboardVisible.value = visible
  }

  @SuppressLint("RestrictedApi")
  override fun setupDialog(dialog: Dialog, style: Int) {
    super.setupDialog(dialog, style)

    dialog.setCanceledOnTouchOutside(false)

    val behavior = (dialog as BottomSheetDialog).behavior

    behavior.isHideable = false
    behavior.isDraggable = false

    dialog.setOnKeyListener(this::onKeyPress)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.Primer_BottomSheet)
    viewModel = PrimerViewModel.getInstance(requireActivity())
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_bottom_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    KeyboardVisibilityEvent.subscribe(
      requireDialog().window!!.decorView,
      viewLifecycleOwner,
      this
    )
  }

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    viewModel.setSheetDismissed(true)
  }

  private fun onKeyPress(dialog: DialogInterface, keyCode: Int, event: KeyEvent?): Boolean {
    val isBackKeyUp = keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_DOWN

    if (!isBackKeyUp) {
      return false
    }

    val canGoBack = childFragmentManager.backStackEntryCount > 0

    if (canGoBack) {
      childFragmentManager.popBackStack()
    } else {
      dialog.dismiss()
    }

    return true
  }

  companion object {
    @JvmStatic
    fun newInstance() = CheckoutSheetFragment()
  }
}