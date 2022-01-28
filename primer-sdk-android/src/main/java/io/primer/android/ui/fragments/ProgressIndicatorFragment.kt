package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.primer.android.PrimerTheme
import io.primer.android.databinding.FragmentProgressBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.ui.extensions.autoCleaned
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class ProgressIndicatorFragment : Fragment(), DIAppComponent {

    private val theme: PrimerTheme by inject()
    private var binding: FragmentProgressBinding by autoCleaned()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.indeterminateDrawable.setTint(
            theme.primaryColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
    }

    companion object {

        fun newInstance(): ProgressIndicatorFragment = ProgressIndicatorFragment()
    }
}
