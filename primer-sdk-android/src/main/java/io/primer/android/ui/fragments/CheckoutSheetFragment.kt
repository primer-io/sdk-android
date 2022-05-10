package io.primer.android.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.primer.android.R
import io.primer.android.PrimerTheme
import io.primer.android.di.DIAppComponent
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.ui.KeyboardVisibilityEvent
import io.primer.android.viewmodel.PrimerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension

@ExperimentalCoroutinesApi
@KoinApiExtension
internal class CheckoutSheetFragment :
    BottomSheetDialogFragment(),
    KeyboardVisibilityEvent.OnChangedListener,
    DIAppComponent {

    companion object {

        @JvmStatic
        fun newInstance() = CheckoutSheetFragment()
    }

    private val theme: PrimerTheme by inject()
    private val viewModel: PrimerViewModel by activityViewModels()

    override fun onKeyboardVisibilityChanged(visible: Boolean) {
        viewModel.setKeyboardVisibility(visible)
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        dialog.setOnKeyListener(this::onKeyPress)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = requireDialog().window ?: return
        KeyboardVisibilityEvent.subscribe(
            window.decorView,
            viewLifecycleOwner,
            this
        )
    }

    override fun onStart() {
        super.onStart()
        if (theme.windowMode == PrimerTheme.WindowMode.FULL_HEIGHT) {
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
}
