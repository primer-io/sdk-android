package io.primer.android.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.primer.android.R
import io.primer.android.UniversalCheckoutTheme
import io.primer.android.di.DIAppComponent
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
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

    companion object {

        private const val NO_VERTICAL_PADDING = "NO_VERTICAL_PADDING"

        @JvmStatic
        fun newInstance(noVerticalPadding: Boolean = false) = CheckoutSheetFragment().apply {
            arguments = Bundle().apply {
                putBoolean(NO_VERTICAL_PADDING, noVerticalPadding)
            }
        }
    }

    private val theme: UniversalCheckoutTheme by inject()

    private lateinit var viewModel: PrimerViewModel
    private lateinit var backStackChangedListener: FragmentManager.OnBackStackChangedListener

    override fun onKeyboardVisibilityChanged(visible: Boolean) {
        viewModel.keyboardVisible.value = visible
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backStackChangedListener = FragmentManager.OnBackStackChangedListener {
            childFragmentManager.removeOnBackStackChangedListener(backStackChangedListener)

            val padding =
                resources.getDimensionPixelSize(R.dimen.primer_checkout_sheet_padding_vert)

            view?.findViewById<View>(R.id.checkout_sheet_content)
                ?.updatePadding(top = padding, bottom = padding)
        }

        viewModel = PrimerViewModel.getInstance(requireActivity())
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

        if (arguments?.getBoolean(NO_VERTICAL_PADDING) == true) {
            view.findViewById<View>(R.id.checkout_sheet_content)
                .updatePadding(top = 0, bottom = 0)

//            backStackChangedListener = FragmentManager.OnBackStackChangedListener {
//                backStackChangedListener?.let {
//                    childFragmentManager.removeOnBackStackChangedListener(it)
//                }
//
//                val padding =
//                    resources.getDimensionPixelSize(R.dimen.primer_checkout_sheet_padding_vert)
//
//                view.findViewById<View>(R.id.checkout_sheet_content)
//                    .updatePadding(top = padding, bottom = padding)
//            }
//                .also { childFragmentManager.addOnBackStackChangedListener(it) }
            childFragmentManager.addOnBackStackChangedListener(backStackChangedListener)
        }

        val window = requireDialog().window ?: return
        KeyboardVisibilityEvent.subscribe(
            window.decorView,
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
}
