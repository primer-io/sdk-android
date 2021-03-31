package io.primer.android.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.primer.android.R
import io.primer.android.UniversalCheckoutTheme
import io.primer.android.di.DIAppComponent
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.logging.Logger
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.ui.KeyboardVisibilityEvent
import io.primer.android.viewmodel.PrimerViewModel
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class CheckoutSheetFragment :
    BottomSheetDialogFragment(),
    KeyboardVisibilityEvent.OnChangedListener,
    DIAppComponent {

    private lateinit var viewModel: PrimerViewModel
    private val theme: UniversalCheckoutTheme by inject()

    override fun onKeyboardVisibilityChanged(visible: Boolean) {
        viewModel.keyboardVisible.value = visible
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        // FIXME why are we overriding this?
        // dialog.setCanceledOnTouchOutside(false)
        // val behavior = (dialog as BottomSheetDialog).behavior
        // behavior.isHideable = false
        // behavior.isDraggable = false

        dialog.setOnKeyListener(this::onKeyPress)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // FIXME why are we applying the style this way??
        // setStyle(STYLE_NORMAL, R.style.Primer_BottomSheet)
        viewModel = PrimerViewModel.getInstance(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        KeyboardVisibilityEvent.subscribe(
            requireDialog().window!!.decorView,
            viewLifecycleOwner,
            this
        )
    }

    override fun onStart() {
        super.onStart()
        if (theme.windowMode == UniversalCheckoutTheme.WindowMode.FULL_HEIGHT) {
            setFullHeight()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.DISMISSED_BY_USER))
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.DISMISSED_BY_USER))
    }

    private fun onKeyPress(dialog: DialogInterface, keyCode: Int, event: KeyEvent?): Boolean {
        val isBackKeyUp = keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_DOWN

        if (!isBackKeyUp) {
            return false
        }

        val canGoBack = childFragmentManager.backStackEntryCount > 0

        if (canGoBack) {
            childFragmentManager.popBackStackImmediate()
        } else {
            EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.DISMISSED_BY_USER))
            dialog.dismiss()
        }

        return true
    }

    private fun setFullHeight() {
        dialog?.findViewById<View>(R.id.design_bottom_sheet)?.let {
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        view?.let { view ->
            view.parent?.let { viewParent ->

                view.post {
                    val parent = viewParent as View
                    val params = parent.layoutParams as CoordinatorLayout.LayoutParams
                    val behavior = params.behavior as BottomSheetBehavior<*>

                    behavior.peekHeight = view.measuredHeight
                }
            }
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = CheckoutSheetFragment()
    }
}
