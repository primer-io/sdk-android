package io.primer.android.ui.fragments.base

import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.R
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.ui.components.PrimerToolbar
import io.primer.android.ui.fragments.CheckoutSheetFragment
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.viewmodel.PrimerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal abstract class BaseFragment : Fragment(), DISdkComponent {
    protected val primerViewModel by activityViewModels<PrimerViewModel>()

    protected val theme: PrimerTheme by inject()

    private var toolbar: PrimerToolbar? = null

    protected fun adjustBottomSheetState(state: Int) {
        val behaviour = getBottomSheetBehavior()
        behaviour?.state = state
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar = (parentFragment as? CheckoutSheetFragment)?.view?.findViewById(R.id.toolbar)
        toolbar?.visibility = View.GONE
    }

    protected fun getToolbar(): PrimerToolbar? {
        return toolbar?.apply {
            isVisible = true
        }
    }

    private fun getBottomSheetBehavior(): BottomSheetBehavior<*>? {
        if (parentFragment !is CheckoutSheetFragment) return null
        val parent = (parentFragment as CheckoutSheetFragment).view?.parent as View
        return (parent.layoutParams as CoordinatorLayout.LayoutParams)
            .behavior as BottomSheetBehavior
    }
}
