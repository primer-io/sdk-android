package io.primer.android.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.primer.android.R
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.di.extension.activityViewModel
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.PrimerViewModelFactory
import io.primer.android.viewmodel.ViewStatus
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
    override fun setupDialog(
        dialog: Dialog,
        style: Int,
    ) {
        super.setupDialog(dialog, style)
        (dialog as ComponentDialog).onBackPressedDispatcher.addCallback(this, true) {
            val canGoBack = childFragmentManager.backStackEntryCount > 0

            if (canGoBack) {
                childFragmentManager.commit {
                    /*
                    Remove all fragments which aren't tied to a back stack entry so that they don't cause visual
                    artifacts.
                     */
                    childFragmentManager.fragments.forEach(::remove)
                }
                childFragmentManager.popBackStackImmediate()
            } else {
                viewModel.setViewStatus(ViewStatus.Dismiss)
                dialog.dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_bottom_sheet, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
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
        viewModel.setViewStatus(ViewStatus.Dismiss)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.setViewStatus(ViewStatus.Dismiss)
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

    private val disableBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing
            }
        }

    fun disableDismiss(disabled: Boolean) {
        (dialog as? ComponentDialog)?.apply {
            if (disabled) {
                onBackPressedDispatcher.addCallback(
                    this,
                    disableBackPressedCallback,
                )
            } else {
                disableBackPressedCallback.remove()
            }
        }

        dialog?.apply {
            setCancelable(!disabled)
            setCanceledOnTouchOutside(!disabled)
        }
    }
}
