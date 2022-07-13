package io.primer.android.ui.fragments.base

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.di.DIAppComponent
import io.primer.android.ui.fragments.CheckoutSheetFragment
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.viewmodel.PrimerViewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal abstract class BaseFragment : Fragment(), DIAppComponent {

    protected val primerViewModel by activityViewModels<PrimerViewModel>()

    protected val theme: PrimerTheme by inject()

    protected fun adjustBottomSheetState(state: Int) {
        val behaviour = getBottomSheetBehavior()
        behaviour?.state = state
    }

    private fun getBottomSheetBehavior(): BottomSheetBehavior<*>? {
        if (parentFragment !is CheckoutSheetFragment) return null
        val parent = (parentFragment as CheckoutSheetFragment).view?.parent as View
        return (parent.layoutParams as CoordinatorLayout.LayoutParams)
            .behavior as BottomSheetBehavior
    }
}
