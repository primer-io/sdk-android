package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.core.di.extensions.viewModel
import io.primer.android.databinding.FragmentInitializingBinding
import io.primer.android.presentation.base.BaseViewModel
import io.primer.android.presentation.base.BaseViewModelFactory
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.settings.PrimerTheme

internal class InitializingFragment : Fragment(), DISdkComponent {
    private val theme: PrimerTheme by inject()
    private var binding: FragmentInitializingBinding by autoCleaned()

    private val viewModel: BaseViewModel by viewModel<BaseViewModel, BaseViewModelFactory>()

    companion object {
        @JvmStatic
        fun newInstance() = InitializingFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentInitializingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.indeterminateDrawable.setTint(
            theme.primaryColor.getColor(
                requireContext(),
                theme.isDarkMode,
            ),
        )
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.VIEW,
                ObjectType.LOADER,
                Place.SDK_LOADING,
            ),
        )
    }
}
