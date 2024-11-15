package io.primer.android.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentDialog
import androidx.activity.addCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.primer.android.R
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.activityViewModel
import io.primer.android.di.extension.inject
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.CheckoutExitReason
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.PrimerViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class CheckoutSheetFragment :
    BottomSheetDialogFragment(),
    DISdkComponent {

    companion object {

        @JvmStatic
        fun newInstance() = CheckoutSheetFragment()
    }

    private val theme: PrimerTheme by inject()
    private val viewModel: PrimerViewModel by
    activityViewModel<PrimerViewModel, PrimerViewModelFactory>()

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        (dialog as ComponentDialog).onBackPressedDispatcher.addCallback(this, true) {
            val canGoBack = childFragmentManager.backStackEntryCount > 0

            if (canGoBack) {
                childFragmentManager.popBackStackImmediate()
            } else {
                EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.DISMISSED_BY_USER))
                dialog.dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            viewModel.setKeyboardVisibility(imeVisible)
            insets
        }
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

    fun disableDismiss(disabled: Boolean) {
        dialog?.apply {
            setCancelable(!disabled)
            setCanceledOnTouchOutside(!disabled)

            setOnKeyListener(
                if (disabled) {
                    { _, keyCode, event ->
                        keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
                    }
                } else {
                    null
                }
            )

            window?.apply {
                if (disabled) {
                    setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                } else {
                    clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }
        }
    }
}
